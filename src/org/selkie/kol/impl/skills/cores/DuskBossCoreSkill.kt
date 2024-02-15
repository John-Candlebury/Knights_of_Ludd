package org.selkie.kol.impl.skills.cores

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.LevelBasedEffect
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.SkillSpecAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier
import com.fs.starfarer.api.combat.listeners.WeaponRangeModifier
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.combat.CombatUtils
import org.selkie.kol.impl.hullmods.NinmahBoss.NinmahAIScript
import org.selkie.kol.impl.skills.cores.BaseCoreOfficerSkill

class DuskBossCoreSkill : BaseCoreOfficerSkill() {

    var modID = "zea_dusk_boss_skill"

    override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
        return LevelBasedEffect.ScopeDescription.PILOTED_SHIP
    }

    override fun createCustomDescription(stats: MutableCharacterStatsAPI?,  skill: SkillSpecAPI?, info: TooltipMakerAPI?,  width: Float) {
        info!!.addSpacer(2f)
        info!!.addPara("Improves the autopilots ability to maneuver ships with phase cloaks.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info!!.addPara("Removes the activation cost of phase cloak.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info!!.addPara("-25%% flux generated by active phase cloak.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        //info!!.addPara("-20%% phase cloak cooldown.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info.addSpacer(2f)
    }

    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {
        if (stats!!.entity is ShipAPI) {
            var ship = stats.entity as ShipAPI

            if (ship.hullSpec.hints.contains(ShipHullSpecAPI.ShipTypeHints.PHASE) || ship.hullSpec.shipDefenseId == "phasecloak") {
                if (!ship.hasListenerOfClass(NinmahAIScript::class.java)) {
                    ship.addListener(NinmahAIScript(ship))
                }
            }
        }

        stats.phaseCloakUpkeepCostBonus.modifyMult(modID, 0.75f)
        //stats.phaseCloakCooldownBonus.modifyMult(modID, 0.80f)
        stats.phaseCloakActivationCostBonus.modifyMult(modID, 0f)
    }

    override fun unapply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?) {
        if (stats!!.entity is ShipAPI) {
            var ship = stats.entity as ShipAPI
        }

    }
}