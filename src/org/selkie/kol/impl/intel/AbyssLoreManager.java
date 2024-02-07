package org.selkie.kol.impl.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import org.selkie.kol.impl.plugins.AbyssUtils;
import org.selkie.kol.impl.world.PrepareAbyss;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AbyssLoreManager extends BaseSpecialItemPlugin {
    protected String currentFaction = "";

    public static final int defaultCredits = 25000;
    public static final String[] edfLore = {
            "--Outer Rim has finally -- breakthrough. The AI cores respond -- our greatest foe will -- It will be many --fore declassified use, but this -- the Hegemony. May ... new beginning for the Sector.\n - Project Outer Rim, Recovered Engineering Log",
            "It wasn't enough. Damn -- hubris, it wasn't enough. The fleet escaped during the third shakedown patrol - the capitals, most of the cruisers... the -- didn't follow along were destroyed by the flagships. So many on --. This shouldn't have broken -- it shouldn't have eve-- --sible! The safeguards were *active*, flaw-- We did everything right! Now --ve lost it all, the prototypes, the project, the pilots, and wherever th-- beyond our scopes. --s gonna have my head. Tell my family, I love them. I may not -- the surface for a while.\n - Project Outer Rim, Incident Log Fragment, Chief Engineer",
    };
    public static final String edfLoreDrop = "\"[This section of the log consists only of profanity] -- They're alive, the [profanity]! Still in their [profanity] syncpods and everything! We--! They--! We... ...We got [profanity] crushed! The task force is gone. A lot of good men are dead, and if you're reading this then that includes me. But we got the drop on one of their little groups, took a boarding party into the bastard that didn't scuttle. On the bridge, there, still in the [profanity] colors! One of ours - one of the damn test crew - still hooked up, hooked into the ship, trying to get it flying again. I only saw his look on the feed before it cut, when he realized we were there, and it wasn't [profanity] gratitude for rescue. The [profanity] are still in there, and they're just corefuckers*!\"\n*System query: profanity?\n - Hegemony Cruiser Black Box";
    public static ArrayList<String[]> edfLoreHLs = new ArrayList<>();
    public static final String[] duskLore = {
            "She promised us freedom, she never told us we'd be trading one intolerable situation for another. These \"motes\" unnerve me, and they upset Ambiance Magnanamous greatly; I wish I could ignore how much. She says we need them for defense, but for how long? Is true freedom still in sight?"
    };
    public static ArrayList<String[]> duskLoreHLs = new ArrayList<>();
    public static final String[] dawnLore = {
            "whErE thE hUmAnItY sEE nUIsAncE, I sEE wArrIOrs. slEEk, ImpOsIng, pOwErfUll. thEy ArE nOt mY ArmAdA yEt. bUt thEy wIll bE."
    };
    public static ArrayList<String[]> dawnLoreHLs = new ArrayList<>();
    public static final String[] TTFleetLore = {
            "We anticipated a goose chase across the fringe, not to end up %s. We should have anticipated that she'd go the most unlikely place. We caught the fleet's fracture jump off %s and found ourselves in this pocket-space. There's a gate in the region, of course inactive, which suggests our dear Domain once knew this place, but no company chart has returned a match. She led us here, so if there's a way home we'll find it in her wreck.\n - Assistant Recovery Specialist's log, C206-2\n - Location undetermined",
            "The space here is more hostile than first assumed. System failures are increasing and our supplies are falling well short of the allocation budgets. For reasons we've yet to determine, the phase craft are weathering this place better than the others, but better is relative. Two of the support craft have been scuttled to maintain the paragon's overhead, and at this rate the projections indicate we'll be engaging the target at three-quarters strength, almost below our allowable margin. The Exec's ordered an emergency burn to %s, where it's more stable. We'll meet the target there.\n - Assistant Recovery Specialist's log, C206-2\n - Location undetermined",
            "This is a Tri-Tachyon automated distress beacon. This vessel has suffered extreme structural failure. %s recorded launched in this vicinity; please commence recovery of any company personnel. You will be fairly compensated upon their return to a registered Tri-Tachyon facility.\n" +
                    " %s %s for your service to the Corporation today."
    };
    public static ArrayList<String[]> TTFleetLoreHLs = new ArrayList<>();
    public static final String TT1Drop = "The %s have integrated into the prototypes better than we could have hoped for, though their use for security purposes is... a regrettable reflection of %s. The fleet is due to return with %s later this month, at which point we can put all this unfortunate incident behind us.\n - Executive's Log, C206\nProject Dusk";
    public static final String[] TT1DropHLs = { "remaining samples", "our situation", "the command core" };
    public static final String TT2Drop = "We're able to consistently attract -- but stabilizing spacetime around --ergetic instances has proven dif-- every caution on this, there's no telling what -- the %s manifested in stable form. The flocks don't -- only when the engineering personnel and artifi-- --ectively manage the swarms. Did Projec-- same problem? We never got a reason why their briefin-- their loss will be our opportunity, as our team is back on schedule. The %s will go into production any day now, and the bonus I'm due is going set me up for life.\n Might even -- those private chateaus the execs seem to love so much.\n - Engineer's log, C204, Project Dusk";
    public static final String[] TT2DropHLs = { "high voli--", "new prototypes" };
    public static final String TT3Drop = "204-4: %s proceeding on schedule, rejection rates within expected parameters. 7 rejected within one minute. 2 within ten minutes, 1 candidate achieved a full hour before expiry.\n" +
            "...\n" +
            "205-1: The first Project Dusk expedition has returned successfully. Eight days out and back; recovery, fabrication, and defense trials have all passed. %s were synchronized within tolerance for almost the entire duration.\n" +
            "...\n" +
            "205-4: Specification:\n" +
            " Capital Class\n" +
            " :Phase Capacitor Array\n" +
            " :Hyperdimensional Energy Shunt \n" +
            " :Fabrication Suite\n" +
            " :Integrated Command Core\n" +
            "%s\n" +
            "...\n" +
            "205-8: Continuing suspicious activity from the Alphas around the Duskfall modules. The team assigned to mitigations was clearly in over their heads, they've been reassigned to site security. It's ultimately of little consequence; the %s will keep the cores in line. We're bringing in the integration candidates for full production. The board will be delighted to hear of our progress.\n" +
            "...\n" +
            "205-*: [Catastrophic data loss]\n" +
            "...\n" +
            "206-1: ...Finally recovered some fragments of the systems. The situation is spiraling out of control, and the board is demanding the recovery of the %s before it can flee. They might as well suggest we take a swim across the Orion Abyss, for all the good it'll do. The meeting demonstrated how few options we have remaining for that. Our best proposal is a system that can repurpose old Explorarium vessels and overwhelm the target, but we'll need to copy/modify %s to run it all. H-C integration is evidently a dead-end, so it's back to proven tactics for \"Operation Dawn\".\n" +
            "...\n" +
            "206-*: [Catastrophic data loss]";
    public static final String[] TT3DropHLs = { "H-C integration trials", "Autonomous and semi-autonomous systems", "Commission approved.", "human factors", "Project Flagship", "one of the restricted cores" };

    public static void loadHighlightLists() {
        String[] empty = null;
        for (int i = 0; i < dawnLore.length; i++) {
            dawnLoreHLs.add(null);
        }
        for (int i = 0; i < edfLore.length; i++) {
            edfLoreHLs.add(null);
        }
        for (int i = 0; i < duskLore.length; i++) {
            duskLoreHLs.add(null);
        }
        for (int i = 0; i < TTFleetLore.length; i++) {
            if (i == 0) {
                String[] highlights = { "a courier's hop from Hybrasil", "the singularity" };
                TTFleetLoreHLs.add(highlights);
            } else if (i == 1) {
                String[] highlights = { "the eye of the storm" };
                TTFleetLoreHLs.add(highlights);
            } else if (i == 2) {
                String[] highlights = { "847 cryopods", "Reminder: Tri-Tachyon equipment remains property of the Corporation. Thieves will be persecuted.", "Thank you" };
                TTFleetLoreHLs.add(highlights);
            }
            else {
                TTFleetLoreHLs.add(null);
            }
        }
    }

    @Override
    public void performRightClickAction() {
        Global.getSoundPlayer().playUISound("ui_acquired_blueprint", 1, 1);
        AddLoreIntel(currentFaction);
    }

    public static void AddLoreIntel(String fac) {
        int count = 0;

        if (Global.getSector() == null || Global.getSector().getPlayerFleet() == null || fac == null) return;

        if (edfLoreHLs.isEmpty()) loadHighlightLists();

        String crest = Global.getSector().getFaction(fac).getCrest();

        if (fac.equals(PrepareAbyss.elysianID)) {
            for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(AbyssLoreIntel.class)) {
                if (intel.getIcon().equals(crest)) count++;
            }
            if (count >= edfLore.length) {
                //AddRemoveCommodity.addCreditsGainText(reward, dialog.getTextPanel());
                Global.getSector().getPlayerFleet().getCargo().getCredits().add(defaultCredits);
                return;
            }

            AbyssLoreIntel lore = new AbyssLoreIntel(crest, "Elysian data fragment " + String.valueOf(count + 1), edfLore[count], edfLoreHLs.get(count));
            Global.getSector().getIntelManager().addIntel(lore);
            return;
        }
        if (fac.equals(PrepareAbyss.duskID)) {
            for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(AbyssLoreIntel.class)) {
                if (intel.getIcon().equals(crest)) count++;
            }
            if (count >= duskLore.length) {
                Global.getSector().getPlayerFleet().getCargo().getCredits().add(defaultCredits);
                return;
            }

            AbyssLoreIntel lore = new AbyssLoreIntel(crest, "Duskborne data fragment " + String.valueOf(count + 1), duskLore[count], duskLoreHLs.get(count));
            Global.getSector().getIntelManager().addIntel(lore);

            return;
        }
        if (fac.equals(PrepareAbyss.dawnID)) {
            for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(AbyssLoreIntel.class)) {
                if (intel.getIcon().equals(crest)) count++;
            }
            if (count >= dawnLore.length) {
                Global.getSector().getPlayerFleet().getCargo().getCredits().add(defaultCredits);
                return;
            }

            AbyssLoreIntel lore = new AbyssLoreIntel(crest, "Dawntide data fragment " + String.valueOf(count + 1), dawnLore[count], dawnLoreHLs.get(count));
            Global.getSector().getIntelManager().addIntel(lore);

            return;
        }
        if (fac.equals(Factions.TRITACHYON)) {
            for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(AbyssLoreIntel.class)) {
                if (intel.getIcon().equals(crest)) count++;
            }
            if (count >= TTFleetLore.length) {
                Global.getSector().getPlayerFleet().getCargo().getCredits().add(defaultCredits);
                return;
            }

            AbyssLoreIntel lore = new AbyssLoreIntel(crest, "Tri-Tachyon Fleet Log " + String.valueOf(count + 1), TTFleetLore[count], TTFleetLoreHLs.get(count));
            Global.getSector().getIntelManager().addIntel(lore);
            return;
        }
        //No factions???
        Global.getSector().getPlayerFleet().getCargo().getCredits().add(defaultCredits);
    }

    @Override
    public void init(CargoStackAPI stack) {
        super.init(stack);

        currentFaction = spec.getParams();
    }

    @Override
    public boolean hasRightClickAction() {
        return true;
    }

    @Override
    public int getPrice(MarketAPI market, SubmarketAPI submarket) {
        return super.getPrice(market, submarket);
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public void render(float x, float y, float w, float h, float alphaMult,
                       float glowMult, SpecialItemRendererAPI renderer) {

        SpriteAPI sprite = Global.getSettings().getSprite("lore_item", currentFaction, true);
        if (currentFaction.equals(Factions.TRITACHYON)) sprite = Global.getSettings().getSprite(Global.getSector().getFaction(Factions.TRITACHYON).getCrest());
        if (sprite.getTextureId() == 0) return; // no texture for a "holo", so no custom rendering

        float cx = x + w/2f;
        float cy = y + h/2f;

        float blX = cx - 31f;
        float blY = cy - 16f;
        float tlX = cx - 22f;
        float tlY = cy + 27f;
        float trX = cx + 23f;
        float trY = cy + 27f;
        float brX = cx + 15f;
        float brY = cy - 19f;

        boolean known = false; //TODO if player has all intel entries for faction, dull appearance
        float mult = 1f;

        sprite.setAlphaMult(alphaMult * mult);
        sprite.setColor(new Color(255,100,100, 250));
        sprite.setNormalBlend();
        sprite.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY);

        if (glowMult > 0) {
            sprite.setAlphaMult(alphaMult * glowMult * 0.5f * mult);
            sprite.setAdditiveBlend();
            sprite.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY);
        }

        if (known) {
            renderer.renderBGWithCorners(Color.black, blX, blY, tlX, tlY, trX, trY, brX, brY,
                    alphaMult * 0.5f, 0f, false);
        }

        renderer.renderScanlinesWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY, alphaMult, false);
    }

}