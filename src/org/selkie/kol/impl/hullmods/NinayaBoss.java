package org.selkie.kol.impl.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.skills.NeuralLinkScript;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.*;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import org.selkie.kol.Utils;
import org.selkie.kol.combat.StarficzAIUtils;
import org.selkie.kol.impl.helpers.ZeaUtils;

import java.awt.*;


public class NinayaBoss extends BaseHullMod {
    public static class NinayaBossPhaseTwoScript implements AdvanceableListener, HullDamageAboutToBeTakenListener {
        public boolean phaseTwo = false;
        public CombatEngineAPI engine;
        public float phaseTwoTimer = 0f;

        public static final float MAX_TIME = 8f;
        public ShipAPI ship;
        public ShipAPI escortA = null, escortB = null;
        public String id = "boss_phase_two_modifier";
        public Utils.FogSpawner escortAFog, escortBFog;
        public NinayaBossPhaseTwoScript(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public boolean notifyAboutToTakeHullDamage(Object param, ShipAPI ship, Vector2f point, float damageAmount) {
            float hull = ship.getHitpoints();
            if (damageAmount >= hull && !phaseTwo) {
                phaseTwo = true;
                escortAFog = new Utils.FogSpawner();
                escortBFog = new Utils.FogSpawner();
                ship.setHitpoints(1f);
                StarficzAIUtils.applyDamper(ship, id, 1);
                ship.getMutableStats().getPeakCRDuration().modifyFlat(id, ship.getHullSpec().getNoCRLossSeconds());
                shipSpawnExplosion(ship, ship.getShieldRadiusEvenIfNoShield(), ship.getLocation());
                float timeMult = ship.getMutableStats().getTimeMult().getModifiedValue();
                Global.getCombatEngine().addFloatingTextAlways(ship.getLocation(),"<REQUESTING REINFORCEMENTS>", NeuralLinkScript.getFloatySize(ship), Color.magenta,
                        ship, 16f * timeMult, 3.2f/timeMult, 4f/timeMult, 0f, 0f,1f);
                return true;
            } else if(phaseTwo && phaseTwoTimer < MAX_TIME){
                return true;
            }

            return false;
        }

        @Override
        public void advance(float amount) {
            engine = Global.getCombatEngine();
            float armorRegen = 0.8f;
            float hpRegen = 0.6f;



            if(phaseTwo && phaseTwoTimer < MAX_TIME){

                phaseTwoTimer += amount;

                if (phaseTwoTimer > MAX_TIME) {
                    StarficzAIUtils.unapplyDamper(ship, id);
                    StarficzAIUtils.unapplyDamper(escortA, id);
                    StarficzAIUtils.unapplyDamper(escortB, id);
                    return;
                }

                // force phase, mitigate damage, regen hp/armor, vent flux, reset ppt/ cr


                ship.getFluxTracker().setHardFlux(0f);
                ship.setHitpoints(Misc.interpolate(1f, ship.getMaxHitpoints()*hpRegen, phaseTwoTimer/MAX_TIME));
                ArmorGridAPI armorGrid = ship.getArmorGrid();

                for(int i = 0; i < armorGrid.getGrid().length; i++){
                    for(int j = 0; j < armorGrid.getGrid()[0].length; j++){
                        if(armorGrid.getArmorValue(i, j) < armorGrid.getMaxArmorInCell()*armorRegen)
                            armorGrid.setArmorValue(i, j, Misc.interpolate(armorGrid.getArmorValue(i, j), armorGrid.getMaxArmorInCell()*armorRegen, phaseTwoTimer/MAX_TIME));
                    }
                }

                StarficzAIUtils.applyDamper(ship, id, Utils.linMap(1,0, MAX_TIME*5/6, MAX_TIME, phaseTwoTimer));
                StarficzAIUtils.stayStill(ship);
                StarficzAIUtils.holdFire(ship);


                // specially tuned for hyperion ships
                PersonAPI captain = Global.getSettings().createPerson();
                captain.setPortraitSprite("graphics/portraits/portrait_ai3b.png");
                captain.setFaction(Factions.REMNANTS);
                captain.setAICoreId(Commodities.BETA_CORE);
                captain.getStats().setLevel(5);
                //captain.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
                captain.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
                //captain.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
                //captain.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
                captain.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
                //captain.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
                captain.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
                //captain.getStats().setSkillLevel(Skills.BALLISTIC_MASTERY, 2);
                captain.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
                //captain.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
                //captain.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
                captain.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
                //captain.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);

                CombatFleetManagerAPI fleetManager = engine.getFleetManager(ship.getOriginalOwner());
                CombatTaskManagerAPI taskManager = (fleetManager != null) ? fleetManager.getTaskManager(ship.isAlly()) : null;

                if (fleetManager == null) return;


                String escortSpec = "zea_boss_hyperion_Strike";

                CombatFleetManagerAPI.AssignmentInfo assignmentInfo = taskManager.createAssignment(CombatAssignmentType.DEFEND, fleetManager.getDeployedFleetMemberEvenIfDisabled(ship), false);

                float escortFacing = ship.getFacing();
                Vector2f escortASpawn = MathUtils.getPointOnCircumference(ship.getLocation(), 200f, escortFacing + 90);
                Vector2f escortBSpawn = MathUtils.getPointOnCircumference(ship.getLocation(), 200f, escortFacing - 90);

                escortAFog.spawnFog(amount, 25f, escortASpawn);
                escortBFog.spawnFog(amount, 25f, escortBSpawn);

                if(phaseTwoTimer > MAX_TIME*2/3){
                    if (escortA == null) {
                        escortA = fleetManager.spawnShipOrWing(escortSpec, escortASpawn, escortFacing + 90f, 0f, captain);
                        escortA.getMutableStats().getPeakCRDuration().modifyMult("phase_boss_cr", 3);
                        shipSpawnExplosion(escortA, escortA.getShieldRadiusEvenIfNoShield(), escortA.getLocation());
                        taskManager.giveAssignment(fleetManager.getDeployedFleetMemberEvenIfDisabled(escortA), assignmentInfo, false);
                    } else{
                        ship.getFluxTracker().setHardFlux(0f);
                        escortA.giveCommand(ShipCommand.DECELERATE, null, 0);
                        StarficzAIUtils.applyDamper(escortA, id, Utils.linMap(1,0, MAX_TIME*2/3, MAX_TIME, phaseTwoTimer));
                        StarficzAIUtils.stayStill(escortA);
                        StarficzAIUtils.holdFire(escortA);
                    }
                }

                if(phaseTwoTimer > MAX_TIME*5/6){
                    if (escortB == null) {
                        escortB = fleetManager.spawnShipOrWing(escortSpec, escortBSpawn, escortFacing - 90f, 0f, captain);
                        escortA.getMutableStats().getPeakCRDuration().modifyMult("phase_boss_cr", 3);
                        shipSpawnExplosion(escortB, escortB.getShieldRadiusEvenIfNoShield(), escortB.getLocation());
                        taskManager.giveAssignment(fleetManager.getDeployedFleetMemberEvenIfDisabled(escortB), assignmentInfo, false);
                    } else{
                        ship.getFluxTracker().setHardFlux(0f);
                        escortB.giveCommand(ShipCommand.DECELERATE, null, 0);
                        StarficzAIUtils.applyDamper(escortB, id, Utils.linMap(1,0, MAX_TIME*2/3, MAX_TIME, phaseTwoTimer));
                        StarficzAIUtils.stayStill(escortB);
                        StarficzAIUtils.holdFire(escortB);
                    }
                }
            }
        }

        public void shipSpawnExplosion(ShipAPI ship, float size, Vector2f location){
            NegativeExplosionVisual.NEParams p = RiftCascadeMineExplosion.createStandardRiftParams(new Color(80,160,240,255), size);
            p.fadeOut = 0.15f;
            p.hitGlowSizeMult = 0.25f;
            p.underglow = new Color(5,120,180,150);
            p.withHitGlow = false;
            p.noiseMag = 1.25f;
            CombatEntityAPI e = Global.getCombatEngine().addLayeredRenderingPlugin(new NegativeExplosionVisual(p));
            e.getLocation().set(location);

            DamagingExplosionSpec spec = new DamagingExplosionSpec(
                1f,
                    ship.getCollisionRadius(),
                70f,
                100000f,
                50000f,
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                0f,
                0f,
                0f,
                0,
                    new Color(0,0,0,0),
                    new Color(0,0,0,0)
            );
            engine.spawnDamagingExplosion(spec, ship, location, false);
        }
    }

    public static class NinayaAIScript implements AdvanceableListener {
        ShipAPI ship;
        CombatEngineAPI engine;
        Boolean ventingHardflux = false;
        public NinayaAIScript(ShipAPI ship) {
            this.ship = ship;
        }
        @Override
        public void advance(float amount) {
            engine = Global.getCombatEngine();
            if (!ship.isAlive() || ship.getParentStation() != null || engine == null || !engine.isEntityInPlay(ship)) {
                return;
            }

            if (StarficzAIUtils.DEBUG_ENABLED) {
                // specially tuned for hyperion
                PersonAPI captain = Global.getSettings().createPerson();
                captain.setPortraitSprite("graphics/portraits/portrait_ai3b.png");
                captain.setFaction(Factions.REMNANTS);
                captain.setAICoreId(Commodities.BETA_CORE);
                captain.getStats().setLevel(5);
                //captain.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
                captain.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
                //captain.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
                //captain.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
                captain.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
                //captain.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
                captain.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
                //captain.getStats().setSkillLevel(Skills.BALLISTIC_MASTERY, 2);
                captain.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
                //captain.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
                //captain.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
                captain.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
                //captain.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
                ship.setCaptain(captain);
            }

            if (ship.getOwner() != 0 || StarficzAIUtils.DEBUG_ENABLED || true) {
                ship.getMutableStats().getPeakCRDuration().modifyFlat("phase_boss_cr", 1000000);
            }

            // force shields on, there is no situation where shields should be off
            if((!engine.isUIAutopilotOn() || engine.getPlayerShip() != ship)){

                if(!AIUtils.getNearbyEnemies(ship, 1000).isEmpty() && ship.getShield() != null) {
                    if (ship.getShield().isOff())
                        ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
                    else
                        ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
                }
                // make sure to drain flux before approaching again
                if(ventingHardflux){
                    Vector2f point = StarficzAIUtils.getBackingOffStrafePoint(ship);
                    if(point != null)
                        StarficzAIUtils.strafeToPoint(ship, point);
                }
            }


            // back off at high hardflux
            if((ship.getHardFluxLevel() > 0.6f && !AIUtils.canUseSystemThisFrame(ship)) || ship.getHardFluxLevel() > 0.75f){
                ventingHardflux = true;
            }

            // return at low hardflux
            if(ship.getHardFluxLevel() < 0.1f){
                ventingHardflux = false;
            }

            if(ventingHardflux){
                ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.PHASE_ATTACK_RUN, 0.1f); // repurposing an unused flag
                ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.BACK_OFF, 0.1f);
            } else{
                ship.getAIFlags().removeFlag(ShipwideAIFlags.AIFlags.PHASE_ATTACK_RUN);
            }

            // dont overflux via weapons
            if(ship.getFluxLevel() > 0.8f){
                StarficzAIUtils.holdFire(ship);
            }
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        boolean isBoss = ship.getVariant().hasTag(ZeaUtils.BOSS_TAG) || (ship.getFleetMember() != null && (ship.getFleetMember().getFleetData() != null &&
                (ship.getFleetMember().getFleetData().getFleet() != null && ship.getFleetMember().getFleetData().getFleet().getMemoryWithoutUpdate().contains(ZeaUtils.BOSS_TAG))));

        if(isBoss || StarficzAIUtils.DEBUG_ENABLED) {
            if(!ship.hasListenerOfClass(NinayaBossPhaseTwoScript.class)) ship.addListener(new NinayaBossPhaseTwoScript(ship));
            if(!ship.hasListenerOfClass(NinayaAIScript.class)) ship.addListener(new NinayaAIScript(ship));

            String key = "phaseAnchor_canDive";
            Global.getCombatEngine().getCustomData().put(key, true); // disable phase dive, as listener conflicts with phase two script
        }
    }
}
