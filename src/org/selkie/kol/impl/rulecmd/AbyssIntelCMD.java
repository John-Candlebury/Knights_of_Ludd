package org.selkie.kol.impl.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import org.selkie.kol.impl.intel.AbyssLoreIntel;
import org.selkie.kol.impl.intel.AbyssLoreManager;
import org.selkie.kol.impl.intel.TriTachBreadcrumbIntel;
import org.selkie.kol.impl.world.PrepareAbyss;
import org.selkie.kol.impl.world.PrepareDarkDeeds;

import java.util.List;
import java.util.Map;

public class AbyssIntelCMD extends BaseCommandPlugin {
    SectorEntityToken TTStationBoss2;
    SectorEntityToken TTBoss3System = null;

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        String command = params.get(0).getString(memoryMap);
        if (command == null) return false;

        SectorEntityToken entity = dialog.getInteractionTarget();
        if (entity.getMarket() != null && !entity.getMarket().isPlanetConditionMarketOnly()) {
            PlanetAPI planet = entity.getMarket().getPlanetEntity();
            if (planet != null) {
                entity = planet;
            }
        }

        if ("addIntelTTBoss1".equals(command)) {

            AbyssLoreIntel intelLore = new AbyssLoreIntel(Global.getSector().getFaction(Factions.TRITACHYON).getCrest(), "Project Dusk Datacore #3", AbyssLoreManager.TT1Drop, AbyssLoreManager.TT1DropHLs);
            Global.getSector().getIntelManager().addIntel(intelLore, true, dialog.getTextPanel());

            if (Global.getSector().getEntityById("zea_boss_station_tritachyon") != null) {
                TTStationBoss2 = Global.getSector().getEntityById("zea_boss_station_tritachyon");
            } else {
                return false;
            }

            TriTachBreadcrumbIntel intel = new TriTachBreadcrumbIntel("Recovered Tri-Tachyon Navigation Log", "A departure entry pulled from the wreck of the Ninaya, supposedly an active Tri-Tachyon development facility. Whatever it may be out there is no doubt terrifyingly dangerous.", TTStationBoss2);
            Global.getSector().getIntelManager().addIntel(intel, false, dialog.getTextPanel());
            return true;

        } else if ("addIntelTTBoss2".equals(command)) {

            AbyssLoreIntel intelLore = new AbyssLoreIntel(Global.getSector().getFaction(Factions.TRITACHYON).getCrest(), "Project Dusk Datacore #1", AbyssLoreManager.TT2Drop, AbyssLoreManager.TT2DropHLs);
            Global.getSector().getIntelManager().addIntel(intelLore, true, dialog.getTextPanel());

            //Handle ninmah recovery
            for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()) {
                if (member.getVariant().getHullSpec().getBaseHullId().startsWith("zea_boss")) {
                    ShipVariantAPI variant = member.getVariant();
                    if (!variant.hasTag(Tags.SHIP_CAN_NOT_SCUTTLE)) variant.addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
                    if (!variant.hasTag(Tags.SHIP_UNIQUE_SIGNATURE)) variant.addTag(Tags.SHIP_UNIQUE_SIGNATURE);
                    if (variant.hasTag("kol_boss")) variant.removeTag("kol_boss");
                    if (variant.hasTag(Tags.VARIANT_UNBOARDABLE)) variant.removeTag(Tags.VARIANT_UNBOARDABLE);
                }
            }

            for (StarSystemAPI system : Global.getSector().getStarSystems()) {
                if (system.getMemoryWithoutUpdate().contains(PrepareDarkDeeds.TTBOSS3_SYSTEM_KEY)) {
                    TTBoss3System = system.getCenter();
                }
            }
            if (TTBoss3System == null) return false;

            TriTachBreadcrumbIntel intelBC = new TriTachBreadcrumbIntel("Recovered Project Dusk Datacore", "You've uncovered the location of a third black site, far far on the outskirts of the sector. Could it be the end of the line, or only the beginning of the thread?", TTBoss3System);
            Global.getSector().getIntelManager().addIntel(intelBC, false, dialog.getTextPanel());
            return true;

        } else if ("addIntelTTBoss3".equals(command)) {
            AbyssLoreIntel intelLore = new AbyssLoreIntel(Global.getSector().getFaction(Factions.TRITACHYON).getCrest(), "Project Dusk Datacore #2", AbyssLoreManager.TT3Drop, AbyssLoreManager.TT3DropHLs);
            Global.getSector().getIntelManager().addIntel(intelLore, false, dialog.getTextPanel());

            SectorEntityToken LunaSea = null;
            for (StarSystemAPI system : Global.getSector().getStarSystems()) {
                if (system.getName().equals(PrepareAbyss.lunaSeaSysName)) {
                    LunaSea = system.getEntityById("zea_lunasea_four");
                }
            }
            if (LunaSea == null) return false;

            TriTachBreadcrumbIntel intelBC = new TriTachBreadcrumbIntel("Project Dawn Hyperspatial Tracking Link", "A tracking link from the doomed Tri-Tachyon \"Operation Dawn\" to... something. Whatever they produced, the link is still sending telemetry, but the readings are nonsensical. If you want answers, there's nothing to do but seek out the location for yourself", LunaSea);
            Global.getSector().getIntelManager().addIntel(intelBC, false, dialog.getTextPanel());

        } else if ("endMusic".equals(command)) {
            Global.getSoundPlayer().restartCurrentMusic();
            return true;
        }

        return false;
    }
}
