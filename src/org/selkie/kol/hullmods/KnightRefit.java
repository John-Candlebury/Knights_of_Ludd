package org.selkie.kol.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.combat.entities.DamagingExplosion;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicIncompatibleHullmods;
import org.selkie.kol.combat.ShipExplosionListener;
import org.selkie.kol.combat.StarficzAIUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*
- flavor description tl;dr-ing both the limited-charge shielding, and ablative armor modules, reduced main ship hull/armor

- Passives
 - Reduces hull and armor by [orangetext] 40% [/o]. [this is done via shipdata.csv, not hullmod]
 - When ballistic weaponry is installed in composite slots :
    - Increases flux capacity by #/#/#.
    - Increases flux dissipation by #/#/#.

 - Front Armor
     - Rating:
     - Effectiveness:
 - Flank Armor
     - Rating:
     - Effectiveness:
 - Rear Armor
     - Rating:
     - Effectiveness:

 - Ship gains increased speed and maneuverability as armor panels are destroyed.

 - When a module is destroyed, the corresponding deco cover on the main hull is hidden
    */

public class KnightRefit extends BaseHullMod {

    public static final int FLUX_CAP_PER_OP = 25;
    public static final int FLUX_DISS_PER_OP = 5;
    private final String knightRefitID = "knightRefit";
    private final float SPEED_BONUS = 0.25f;
    protected Object STATUSKEY1 = new Object();

    @Override
    public void init(HullModSpecAPI spec) {

    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship.getVariant().hasHullMod("advancedshieldemitter")) MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), "advancedshieldemitter", "kol_refit");
        if (ship.getVariant().hasHullMod("adaptiveshields")) MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), "adaptiveshields", "kol_refit");
        if (ship.getVariant().hasHullMod("frontemitter")) MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), "frontemitter", "kol_refit");
        if (ship.getVariant().hasHullMod("extendedshieldemitter")) MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), "extendedshieldemitter", "kol_refit");

        PersonAPI captain = ship.getOriginalCaptain();
        MutableCharacterStatsAPI stats = captain == null ? null : captain.getFleetCommanderStats();
        float capBonus = 0;
        float dissBonus = 0;
        for(WeaponAPI weapon : ship.getAllWeapons()){
            if (weapon.getSlot().getWeaponType() == WeaponAPI.WeaponType.COMPOSITE ){
                int opCost = (int) weapon.getSpec().getOrdnancePointCost(stats, ship.getMutableStats());
                capBonus += opCost* FLUX_CAP_PER_OP;
                dissBonus += opCost* FLUX_DISS_PER_OP;
            }
        }
        ship.getMutableStats().getFluxCapacity().modifyFlat(id, capBonus);
        ship.getMutableStats().getFluxDissipation().modifyFlat(id, dissBonus);

        if(!ship.hasListenerOfClass(ModuleUnhulker.class)) ship.addListener(new ModuleUnhulker());
        if(!ship.hasListenerOfClass(ShipExplosionListener.class)) ship.addListener(new ShipExplosionListener());
        if(!ship.hasListenerOfClass(ExplosionOcclusionRaycast.class)) ship.addListener(new ExplosionOcclusionRaycast());
    }

    public static class ModuleUnhulker implements HullDamageAboutToBeTakenListener {
        @Override
        public boolean notifyAboutToTakeHullDamage(Object param, ShipAPI ship, Vector2f point, float damageAmount) {
            if(ship.getHitpoints() <= damageAmount) {
                for(ShipAPI module: ship.getChildModulesCopy()){
                    if(!module.hasTag("KOL_moduleDead")){
                        module.setHulk(false);
                        module.addTag("KOL_moduleDead");
                    }
                }
            }
            return false;
        }
    }

    public static class ExplosionOcclusionRaycast implements DamageTakenModifier {
        public static final int NUM_RAYCASTS = 36;
        @Override
        public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            // checking for explosions
            if (param instanceof MissileAPI || param instanceof DamagingExplosion) {
                Vector2f explosionLocation;
                float radius;
                if(param instanceof MissileAPI ){
                    MissileAPI missile = (MissileAPI) param;
                    explosionLocation = missile.getLocation();
                    radius = missile.getSpec().getExplosionRadius();
                } else{
                    DamagingExplosion explosion = (DamagingExplosion) param;
                    explosionLocation = explosion.getLocation();
                    radius = explosion.getCollisionRadius();
                }

                // note down all potential occlusions, skip if nothing is in range
                ShipAPI closestModule = null;
                float closestDistance = Float.POSITIVE_INFINITY;
                List<ShipAPI> potentialOcclusions = new ArrayList<>();
                for (ShipAPI occlusion : ((ShipAPI) target).getChildModulesCopy()){
                    float explosionDistance = Misc.getTargetingRadius(explosionLocation, occlusion, false) + radius;
                    float moduleDistance = MathUtils.getDistanceSquared(explosionLocation, occlusion.getLocation());
                    if(occlusion.getHitpoints() > 0 && moduleDistance < (explosionDistance * explosionDistance)){
                        potentialOcclusions.add(occlusion);
                        if(moduleDistance < closestDistance){
                            closestDistance = moduleDistance;
                            closestModule = occlusion;
                        }
                    }
                }

                if(potentialOcclusions.isEmpty() || closestModule == null) return null;

                // for everything in range ray cast a bunch of lines
                int hitShip = 0;
                int hitModule = 0;
                List<Vector2f> rayEndpoints = MathUtils.getPointsAlongCircumference(explosionLocation, radius, NUM_RAYCASTS, 0f);
                for(Vector2f endpoint : rayEndpoints){
                    float closestDistanceSquared = radius * radius;
                    for(ShipAPI occlusion : potentialOcclusions){
                        Vector2f pointOnModuleBounds = CollisionUtils.getCollisionPoint(explosionLocation, endpoint, occlusion);
                        if(pointOnModuleBounds != null){
                            closestDistanceSquared = Math.min(closestDistanceSquared, MathUtils.getDistanceSquared(explosionLocation, pointOnModuleBounds));
                        }
                    }

                    Vector2f pointOnShipBounds = CollisionUtils.getCollisionPoint(explosionLocation, endpoint, target);
                    if(pointOnShipBounds != null){
                        if(closestDistanceSquared < MathUtils.getDistanceSquared(explosionLocation, pointOnShipBounds)){
                            hitModule++;
                        } else{
                            hitShip++;
                        }
                    }
                }

                // if rays hit nothing skip
                if(hitModule + hitShip == 0) return null;

                // otherwise calculate and apply damage mod
                float maxNegation = closestModule.getHitpoints() + StarficzAIUtils.getCurrentArmorRating(closestModule);

                float damageMult = (float) hitShip /(hitModule + hitShip);

                if (damage.getDamage() * (1 - damageMult) > maxNegation){
                    damage.getModifier().modifyFlat(this.getClass().getName(), -maxNegation);
                } else{
                    damage.getModifier().modifyMult(this.getClass().getName(), damageMult);
                }
                return this.getClass().getName();
            }

            return null;
        }
    }


    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        //
    }

    @Override
    public float getTooltipWidth() {
        return 400f;
    }
    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float HEIGHT = 64f;
        float headingPad = 20f;
        float underHeadingPad = 5f;
        float listPad = 3f;


        Color activeTextColor = Misc.getTextColor();
        Color activePositiveColor = Misc.getPositiveHighlightColor();
        Color activeNegativeColor = Misc.getNegativeHighlightColor();
        Color activeHeaderBannerColor = Misc.getDarkPlayerColor();
        Color activeHighlightColor = Misc.getHighlightColor();

        Color inactiveTextColor = Misc.getGrayColor().darker();
        Color inactivePositiveColor = Misc.getGrayColor().darker();
        Color inactiveNegativeColor = Misc.getGrayColor().darker();
        Color inactiveHeaderBannerColor = Misc.getDarkPlayerColor().darker().darker();
        Color inactiveHighlightColor = Misc.getGrayColor().darker();

        boolean hasComposite = false;
        for(WeaponSlotAPI slot : ship.getVariant().getHullSpec().getAllWeaponSlotsCopy()){
            if (slot.getWeaponType() == WeaponAPI.WeaponType.COMPOSITE){
                hasComposite = true;
                break;
            }
        }
        tooltip.addSectionHeading("Integrated Ballistics", hasComposite ? activeHighlightColor : inactiveHighlightColor,
                hasComposite ? activeHeaderBannerColor : inactiveHeaderBannerColor, Alignment.MID, headingPad);
        TooltipMakerAPI integratedBallistics = tooltip.beginImageWithText(Global.getSettings().getSpriteName("icons", hasComposite ? "kol_composite" : "kol_composite_grey"), HEIGHT);
        integratedBallistics.setBulletedListMode("•");
        integratedBallistics.setBulletWidth(15f);
        integratedBallistics.addPara("Every ordnance point spent on ballistic weapons installed into composite slots increases Flux Capacity by %s, and Flux Dissipation by %s.",
                listPad, hasComposite ? activeTextColor : inactiveTextColor, hasComposite ? activePositiveColor: inactivePositiveColor, ""+FLUX_CAP_PER_OP, ""+FLUX_DISS_PER_OP);
        tooltip.addImageWithText(underHeadingPad);


        boolean hasShield = ship.getShield() != null;
        tooltip.addSectionHeading("Primitive Capacitor Shields", hasShield ? activeHighlightColor : inactiveHighlightColor,
                hasShield ? activeHeaderBannerColor : inactiveHeaderBannerColor, Alignment.MID, headingPad);
        TooltipMakerAPI capacitorShields = tooltip.beginImageWithText(Global.getSettings().getSpriteName("icons", hasShield ? "kol_primshield" : "kol_primshield_grey"), HEIGHT);
        capacitorShields.setBulletedListMode("•");
        capacitorShields.setBulletWidth(15f);
        capacitorShields.addPara("Shields rely on a charge and can only stay online for a max of %s at a time.",
                listPad, hasShield ? activeTextColor : inactiveTextColor, hasShield ? activePositiveColor: inactivePositiveColor, "10 seconds");
        capacitorShields.addPara("Shield charge regenerates while shields are offline.", hasShield ? activeTextColor : inactiveTextColor, listPad);
        if(hasShield){
            capacitorShields.addPara("Shield emitters undergo a forced shutdown when charge reaches %s, and can only be reactivated once recharged to %s.",
                    listPad, new Color[] {activeNegativeColor, activePositiveColor}, "0%", "100%");
        } else{
            capacitorShields.addPara("Shield emitters undergo a forced shutdown when charge reaches %s, and can only be reactivated once recharged to %s.",
                    listPad, inactiveTextColor, inactivePositiveColor, "0%", "100%");
        }

        tooltip.addImageWithText(underHeadingPad);


        ShipVariantAPI variant = Global.getSettings().getVariant(ship.getHullSpec().getBaseHullId() + "_Blank");
        boolean hasModules = variant != null && !variant.getStationModules().isEmpty();
        tooltip.addSectionHeading("Modular Armor", hasModules ? activeHighlightColor : inactiveHighlightColor,
                hasModules ? activeHeaderBannerColor : inactiveHeaderBannerColor, Alignment.MID, headingPad);
        TooltipMakerAPI modularArmor = tooltip.beginImageWithText(Global.getSettings().getSpriteName("icons", hasModules ? "kol_modules" : "kol_modules_grey"), HEIGHT);
        modularArmor.setBulletedListMode("•");
        modularArmor.setBulletWidth(15f);
        modularArmor.addPara("Increases top speed and maneuverability by up to %s as armor panels are destroyed.",
                listPad, hasModules ? activeTextColor : inactiveTextColor, hasModules ? activePositiveColor: inactivePositiveColor, "25%");
        modularArmor.addPara("Armor panels have ablative armor, reducing the effective armor strength for damage reduction calculations to %s of its actual value.",
                listPad, hasModules ? activeTextColor : inactiveTextColor, hasModules ? activeNegativeColor: inactiveNegativeColor, "10%");

        if(hasModules){
            modularArmor.beginTable(
                    Misc.getBasePlayerColor(),
                    Misc.getDarkPlayerColor(),
                    Misc.getBrightPlayerColor(),
                    20f,
                    true,
                    true,
                    new Object[]{"Armor Location", width - 80f * 2 - HEIGHT - 25f, "Hull", 80f, "Armor", 80f});

            // getting the stats of child modules in refit shouldn't have to be this hard
            Pattern kolPattern = Pattern.compile("kol_.+?_[tml][lr]", Pattern.CASE_INSENSITIVE);

            for (String module : variant.getStationModules().values()) {
                Matcher matcher = kolPattern.matcher(module);

                if(matcher.find()){
                    ShipHullSpecAPI hull = Global.getSettings().getHullSpec(matcher.group());
                    float hullMult = getTotalHullMult(ship.getVariant(), hull.getHitpoints());
                    float armorMult = getTotalArmorMult(ship.getVariant(), hull.getArmorRating());

                    Color hullTextColor = hullMult < 0.99f ? Misc.getPositiveHighlightColor() : (hullMult > 1.01f ? Misc.getNegativeHighlightColor() : Misc.getTextColor());
                    Color armorTextColor = armorMult < 0.99f ? Misc.getPositiveHighlightColor() : (armorMult > 1.01f ? Misc.getNegativeHighlightColor() : Misc.getTextColor());

                    modularArmor.addRow(Alignment.MID, Misc.getTextColor(), hull.getHullName(),
                            Alignment.MID, hullTextColor, String.valueOf(Math.round(hull.getHitpoints() / hullMult)),
                            Alignment.MID, armorTextColor, String.valueOf(Math.round(hull.getArmorRating() / armorMult)));
                }
            }
            modularArmor.addTable("-", 0, 4f);

            modularArmor.setBulletedListMode("");
            modularArmor.setBulletWidth(0f);

            modularArmor.addPara("Hold 1 to highlight armor locations, 2 to revert.", Misc.getGrayColor(), 10);
            Color fadeAwayColor = Keyboard.isKeyDown(2) ? new Color(200,200,255, 80) : Color.white;
            if (Keyboard.isKeyDown(2) || Keyboard.isKeyDown(3)) {
                ship.getSpriteAPI().setColor(fadeAwayColor);
                for(ShipEngineControllerAPI.ShipEngineAPI engine : ship.getEngineController().getShipEngines()){
                    EngineSlotAPI engineSlot = engine.getEngineSlot();
                    Color originalCOlor = engineSlot.getColor();
                    engineSlot.setColor(new Color(originalCOlor.getRed(), originalCOlor.getGreen(), originalCOlor.getBlue(), 20));
                }
                for(WeaponAPI weapon : ship.getAllWeapons()){
                    if(Objects.equals(weapon.getSlot().getId(), "PUSHER_PLATE")){
                        weapon.getSprite().setColor(fadeAwayColor);
                    }
                    else if(weapon.getSprite() != null && weapon.isDecorative()){
                        weapon.getSprite().setColor(Keyboard.isKeyDown(2) ? new Color(255,255,255, 0) : Color.white);
                    }
                    else if(weapon.getSprite() != null){
                        weapon.getSprite().setColor(fadeAwayColor);
                        if(weapon.getBarrelSpriteAPI() != null)
                            weapon.getBarrelSpriteAPI().setColor(fadeAwayColor);
                    }
                }
            }
        }
        tooltip.addImageWithText(underHeadingPad);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        //Yoinked whole-cloth from SCY. <3 ya Tarty
        if (ship==null) {
            return;
        }

        if (!ship.isAlive()) {
            removeStats(ship);
            return;
        }


        if(Global.getCombatEngine().isPaused()) return;

        // apply speed boost for main ship and durability buffs to modules from main ships hullmods
        ShipVariantAPI variant = Global.getSettings().getVariant(ship.getHullSpec().getBaseHullId() + "_Blank");
        float modules = variant == null ? 0 : variant.getStationModules().size();

        float alive = 0;
        for(ShipAPI module : ship.getChildModulesCopy()){
            if (module.getHitpoints() <= 0f) continue;
            alive++;
            if(ship.getVariant() == null || module.getVariant() == null) continue;

            float hullmult =  getTotalHullMult(ship.getVariant(), module.getVariant().getHullSpec().getHitpoints());
            float armorMult =  getTotalArmorMult(ship.getVariant(), module.getVariant().getHullSpec().getArmorRating());

            module.getMutableStats().getHullDamageTakenMult().modifyMult("kol_module_parent_hullmods", hullmult);
            module.getMutableStats().getArmorDamageTakenMult().modifyMult("kol_module_parent_hullmods", armorMult);
        }

        if(modules!=0){
            //speed bonus applies linearly
            float speedRatio=1 - (alive / modules);
            applyStats(speedRatio, ship);
        }
    }

    private void removeStats(ShipAPI ship) {
        ship.getMutableStats().getMaxSpeed().unmodify(knightRefitID);
        ship.getMutableStats().getAcceleration().unmodify(knightRefitID);
        ship.getMutableStats().getDeceleration().unmodify(knightRefitID);
        ship.getMutableStats().getMaxTurnRate().unmodify(knightRefitID);
        ship.getMutableStats().getTurnAcceleration().unmodify(knightRefitID);
    }

    private void applyStats(float speedRatio, ShipAPI ship) {
        ship.getMutableStats().getMaxSpeed().modifyMult(knightRefitID, (1 + (speedRatio * SPEED_BONUS)));
        ship.getMutableStats().getAcceleration().modifyMult(knightRefitID, (1 + (speedRatio * SPEED_BONUS)));
        ship.getMutableStats().getDeceleration().modifyMult(knightRefitID, (1 + (speedRatio * SPEED_BONUS)));
        ship.getMutableStats().getMaxTurnRate().modifyMult(knightRefitID, (1 + (speedRatio * SPEED_BONUS)));
        ship.getMutableStats().getTurnAcceleration().modifyMult(knightRefitID, (1 + (speedRatio * SPEED_BONUS)));

        CombatEngineAPI engine = Global.getCombatEngine();
        if(engine.getPlayerShip() == ship && speedRatio > 0.01f){
            String modularIcon = Global.getSettings().getSpriteName("icons", "kol_modules");
            engine.maintainStatusForPlayerShip(STATUSKEY1, modularIcon, "Damaged Modular Armor", "+" + Math.round((speedRatio * SPEED_BONUS * 100)) + " top speed" , false);
        }
    }

    public float getTotalArmorMult(ShipVariantAPI variant, float baseArmor){
        if(variant == null) return 1f;
        Map<String, ArmorEffect> effects = HULLMOD_EFFECTS.get(variant.getHullSize());
        float totalFlat = 0;
        float totalPercent = 0;
        boolean isAblative = true; //TODO: fix this hack
        for(String hullmodID : variant.getHullMods()){
            if(effects.containsKey(hullmodID)){
                totalFlat += effects.get(hullmodID).armorFlat;
                totalPercent += effects.get(hullmodID).armorPercent;
            }
            if(Objects.equals(hullmodID, "ablative_armor")) isAblative = true;
        }
        baseArmor = baseArmor * (isAblative ? 0.1f :1f);
        return baseArmor / (baseArmor + totalFlat + (baseArmor * totalPercent));
    }

    public float getTotalHullMult(ShipVariantAPI variant, float baseHull){
        if(variant == null) return 1f;
        Map<String, ArmorEffect> effects = HULLMOD_EFFECTS.get(variant.getHullSize());
        float totalFlat = 0;
        float totalPercent = 0;
        for(String hullmodID : variant.getHullMods()){
            if(effects.containsKey(hullmodID)){
                totalFlat += effects.get(hullmodID).hullFlat;
                totalPercent += effects.get(hullmodID).hullPercent;
            }
        }
        return baseHull / (baseHull + totalFlat + (baseHull * totalPercent));
    }

    private static final Map<ShipAPI.HullSize, Map<String, ArmorEffect>> HULLMOD_EFFECTS = new HashMap<>();

    static {
        Map<String, ArmorEffect> hullmodMap = new HashMap<>();
        hullmodMap.put(HullMods.REINFORCEDHULL, new ArmorEffect(0,0,0,0.4f));
        hullmodMap.put(HullMods.BLAST_DOORS, new ArmorEffect(0,0,0,0.2f));
        hullmodMap.put(HullMods.INSULATEDENGINE, new ArmorEffect(0,0,0,0.1f));
        hullmodMap.put(HullMods.ARMOREDWEAPONS, new ArmorEffect(0,0.1f,0,0));
        hullmodMap.put(HullMods.COMP_HULL, new ArmorEffect(0,0f,0,-0.2f));
        hullmodMap.put(HullMods.COMP_ARMOR, new ArmorEffect(0,-0.2f,0,0));
        hullmodMap.put(HullMods.COMP_STRUCTURE, new ArmorEffect(0,-0.2f,0,-0.2f));

        Map<String, ArmorEffect> capitalHullmodMap = new HashMap<>(hullmodMap);
        capitalHullmodMap.put(HullMods.HEAVYARMOR, new ArmorEffect(500,0,0,0));
        HULLMOD_EFFECTS.put(ShipAPI.HullSize.CAPITAL_SHIP, capitalHullmodMap);

        Map<String, ArmorEffect> cruiserHullmodMap = new HashMap<>(hullmodMap);
        cruiserHullmodMap.put(HullMods.HEAVYARMOR, new ArmorEffect(400,0,0,0));
        HULLMOD_EFFECTS.put(ShipAPI.HullSize.CRUISER, cruiserHullmodMap);

        Map<String, ArmorEffect> destroyerHullmodMap = new HashMap<>(hullmodMap);
        destroyerHullmodMap.put(HullMods.HEAVYARMOR, new ArmorEffect(300,0,0,0));
        HULLMOD_EFFECTS.put(ShipAPI.HullSize.DESTROYER, destroyerHullmodMap);

        Map<String, ArmorEffect> frigateHullmodMap = new HashMap<>(hullmodMap);
        frigateHullmodMap.put(HullMods.HEAVYARMOR, new ArmorEffect(150,0,0,0));
        HULLMOD_EFFECTS.put(ShipAPI.HullSize.FRIGATE, frigateHullmodMap);

        Map<String, ArmorEffect> fighterHullmodMap = new HashMap<>(hullmodMap);
        fighterHullmodMap.put(HullMods.HEAVYARMOR, new ArmorEffect(75,0,0,0));
        HULLMOD_EFFECTS.put(ShipAPI.HullSize.FIGHTER, fighterHullmodMap);
    }

    public static class ArmorEffect {
        public float armorFlat, armorPercent, hullFlat, hullPercent;
        ArmorEffect(float aF, float aP, float hF, float hP){
            armorFlat = aF; armorPercent = aP; hullFlat = hF; hullPercent = hP;
        }
    }
}
