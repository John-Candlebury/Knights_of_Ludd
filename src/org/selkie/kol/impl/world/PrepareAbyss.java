package org.selkie.kol.impl.world;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain.RingParams;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import org.selkie.kol.impl.terrain.AbyssCorona;
import org.selkie.kol.impl.terrain.AbyssEventHorizon;
import org.selkie.kol.plugins.KOL_ModPlugin;

public class PrepareAbyss {

    public static void generate(SectorAPI sector) {
    	GenerateElysia(sector);
    	GeneratePhaseAbyss(sector);
    	GenerateLunaSea(sector);

        //PrepareForRelations(sector);
    }
    
    public static void GenerateElysia(SectorAPI sector) {
    	
    	int beeg = 1500;
    	double posX = 2600;
    	double posY = 24900;
    	
    	//Variable location
    	//posX = Math.random()%(Global.getSettings().getFloat("sectorWidth")-10000);
    	//posY = Math.random()%(Global.getSettings().getFloat("sectorHeight")-8000);
        
    	StarSystemAPI system = sector.createStarSystem("Elysia");
    	system.getLocation().set((int)posX, (int)posY);
    	system.setBackgroundTextureFilename("data/strings/com/fs/starfarer/api/impl/campaign/you can hear it cant you/our whispers through the void/our song/graphics/backgrounds/abyss_edf_elysiabg.png");
		system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "music_campaign_alpha_site");

		system.addTag(Tags.THEME_HIDDEN);
		system.addTag(Tags.THEME_SPECIAL);
		system.addTag(Tags.THEME_UNSAFE);

		//fancy bg script
		
    	//PlanetAPI elysia = system.initStar("Elysian_Abyss", StarTypes.BLACK_HOLE, beeg, -750f);
		system.initNonStarCenter();
    	PlanetAPI elysia = system.addPlanet("abyss_elysia_abyss", system.getCenter(), "Elysian Abyss", StarTypes.BLACK_HOLE, 0f, beeg, 0f, 100000f);
    	elysia.getSpec().setBlackHole(true);
    	elysia.setName("Elysian Abyss");
    	elysia.applySpecChanges();
    	SectorEntityToken horizon1 = system.addTerrain("kol_eventHorizon", new AbyssEventHorizon.CoronaParams(
    			4000,
				250,
				elysia,
				-10f,
				0f,
				5f)
    		);
    	
    	//runcode org.selkie.kol.impl.world.prepareAbyss.GenerateAbyss(Global.getSector());
    	//runcode Global.getSector().getStarSystem("Elysia").addRingBand(Global.getSector().getStarSystem("Elysia").getStar(), "misc", "rings_dust0", 1620, 0, Color.red, 1620, 3434, 17, Terrain.RING, "Accretion Disk");
		//runcode Global.getSector().getPlayerFleet().addTag("abyss_edf_rulesfortheebutnotforme");

    	system.addRingBand(elysia, "misc", "rings_dust0", 1620, 0, Color.red, 1620, 3124, 17, Terrain.RING, "Accretion Disk");
    	//SectorEntityToken accretion1 = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(5000, 1500, elysia, "Accretion Disk"));
    	//accretion1.addTag(Tags.ACCRETION_DISK);
    	//accretion1.setCircularOrbit(elysia, 0, 0, 10);
    	
    	SectorEntityToken elysian_nebula = Misc.addNebulaFromPNG("data/strings/com/fs/starfarer/api/impl/campaign/you can hear it cant you/our whispers through the void/our song/graphics/terrain/pinwheel_nebula.png",
                                                                    0, 0, // center of nebula
                                                                    system, // location to add to
                                                                    "terrain", "nebula_kol_black", // "nebula_blue", // texture to use, uses xxx_map for map
                                                                    4, 4, StarAge.AVERAGE); // number of cells in texture

    	system.setType(StarSystemType.TRINARY_1CLOSE_1FAR);
    	
    	PlanetAPI gaze = system.addPlanet("abyss_elysia_gaze", elysia, "Watcher", StarTypes.NEUTRON_STAR, 125, 50, 9400, 60);
    	gaze.getSpec().setPulsar(true);
    	gaze.applySpecChanges();
    	system.setSecondary(gaze);
    	
		SectorEntityToken gazeBeam1 = system.addTerrain("kol_pulsarBeam",
				new AbyssCorona.CoronaParams(25000,
						3250,
						gaze,
						150f,
						1f,
						5f)
		);
		gazeBeam1.setCircularOrbit(gaze, 0, 0, 15);

		SectorEntityToken gazeBeam2 = system.addTerrain("kol_pulsarBeam",
				new AbyssCorona.CoronaParams(25000,
						3250,
						gaze,
						150f,
						1f,
						5f)
		);
		gazeBeam2.setCircularOrbit(gaze, 0, 0, 16);
    	
    	PlanetAPI silence = system.addPlanet("abyss_elysia_silence", elysia, "Silence", StarTypes.BLUE_SUPERGIANT, 255, 1666, 18500, 0);
    	system.setTertiary(silence);
    	silence.setFixedLocation(-14000, -16500);
    	
		SectorEntityToken silence_corona = system.addTerrain("kol_corona",
				new AbyssCorona.CoronaParams(11000,
						0,
						silence,
						5f,
						0.1f,
						1f)
		);
		silence_corona.setCircularOrbit(silence, 0, 0, 15);

    	PlanetAPI first = system.addPlanet("abyss_elysia_zealot", elysia, "Zealot", "barren-bombarded", (float)(Math.random() * 360), 100, 4900, 50);
    	PlanetAPI second = system.addPlanet("abyss_elysia_pilgrim", elysia, "Pilgrim", "barren-bombarded", (float)(Math.random() * 360), 100, 4100, 40);
    	PlanetAPI third = system.addPlanet("abyss_elysia_apostate", elysia, "Apostate", "barren-bombarded", (float)(Math.random() * 360), 100, 3300, 30);
    	
    	first.getMarket().addCondition(Conditions.HIGH_GRAVITY);
    	second.getMarket().addCondition(Conditions.HIGH_GRAVITY);
    	third.getMarket().addCondition(Conditions.HIGH_GRAVITY);
        
		SectorEntityToken ring = system.addTerrain(Terrain.RING, new RingParams(456, 3200, null, "Call of the Void"));
		ring.setCircularOrbit(elysia, 0, 0, 100);
		SectorEntityToken ring2 = system.addTerrain(Terrain.RING, new RingParams(456, 3656, null, "Call of the Void"));
		ring2.setCircularOrbit(elysia, 0, 0, 100);
		SectorEntityToken ring3 = system.addTerrain(Terrain.RING, new RingParams(456, 4112, null, "Call of the Void"));
		ring3.setCircularOrbit(elysia, 0, 0, 100);
		SectorEntityToken ring4 = system.addTerrain(Terrain.RING, new RingParams(456, 4678, null, "Call of the Void"));
		ring4.setCircularOrbit(elysia, 0, 0, 100);
		
		system.addAsteroidBelt(elysia, 200, 3128, 256, 20, 20, "kol_asteroidBelt", null);
		system.addAsteroidBelt(elysia, 200, 3516, 512, 30, 30, "kol_asteroidBelt", null);
		system.addAsteroidBelt(elysia, 200, 4024, 1024, 40, 40, "kol_asteroidBelt", null);
		system.addAsteroidBelt(elysia, 200, 4536, 1024, 40, 60, "kol_asteroidBelt", null);

        SectorEntityToken derelict1 = DerelictThemeGenerator.addSalvageEntity(system, "alpha_site_weapons_cache", KOL_ModPlugin.duskID);
        derelict1.setCircularOrbit(elysia, (float)(Math.random() * 360f), 3100, 20);
        SectorEntityToken derelict2 = DerelictThemeGenerator.addSalvageEntity(system, "alpha_site_weapons_cache", KOL_ModPlugin.duskID);
        derelict2.setCircularOrbit(elysia, (float)(Math.random() * 360f), 3400, 25);
        SectorEntityToken derelict3 = DerelictThemeGenerator.addSalvageEntity(system, "alpha_site_weapons_cache", KOL_ModPlugin.duskID);
        derelict3.setCircularOrbit(elysia, (float)(Math.random() * 360f), 3700, 30);
        SectorEntityToken derelict4 = DerelictThemeGenerator.addSalvageEntity(system, "alpha_site_weapons_cache", KOL_ModPlugin.duskID);
        derelict4.setCircularOrbit(elysia, (float)(Math.random() * 360f), 4000, 35);
        SectorEntityToken derelict5 = DerelictThemeGenerator.addSalvageEntity(system, "alpha_site_weapons_cache", KOL_ModPlugin.duskID);
        derelict5.setCircularOrbit(elysia, (float)(Math.random() * 360f), 4400, 40);
    	
        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("abyss_elysia_jp", "First Trial");
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(first, 90, 100, 25);
        jumpPoint.setOrbit(orbit);
        jumpPoint.setRelatedPlanet(first);
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);
        
        system.autogenerateHyperspaceJumpPoints(false, false); //begone evil clouds
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);

		ElysianSeededFleetManager fleets = new ElysianSeededFleetManager(system, 3, 5, 10, 50, 0.95f);
		system.addScript(fleets);

    }

	public static void GeneratePhaseAbyss(SectorAPI sector) {

		// Deep in quasi space, no direct access

		StarSystemAPI system = sector.createStarSystem("Phase Abyss");
		system.setName("Phase Abyss");
		LocationAPI hyper = Global.getSector().getHyperspace();
		system.getLocation().set(-1000, -5000);
		system.addTag(Tags.THEME_HIDDEN);
		system.addTag(Tags.THEME_SPECIAL);
		system.addTag(Tags.THEME_UNSAFE);

		system.setBackgroundTextureFilename("data/strings/com/fs/starfarer/api/impl/campaign/you can hear it cant you/our whispers through the void/our song/graphics/backgrounds/abyss_edf_phasebg.jpg");
		system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "music_campaign_alpha_site");

		SectorEntityToken center = system.initNonStarCenter();
		SectorEntityToken elysian_nebula = Misc.addNebulaFromPNG("data/strings/com/fs/starfarer/api/impl/campaign/you can hear it cant you/our whispers through the void/our song/graphics/terrain/pinwheel_nebula.png",
				0, 0, // center of nebula
				system, // location to add to
				"terrain", "nebula", // "nebula_blue", // texture to use, uses xxx_map for map
				4, 4, StarAge.AVERAGE); // number of cells in texture

		system.setLightColor(new Color(225,170,255,255)); // light color in entire system, affects all entities
		new AbyssBackgroundWarper(system, 8, 0.125f);

		system.generateAnchorIfNeeded();

		ElysianSeededFleetManager fleets = new ElysianSeededFleetManager(system, 3, 5, 10, 50, 0.95f);
		system.addScript(fleets);
	}

	public static void GenerateLunaSea(SectorAPI sector) {

		StarSystemAPI system = sector.createStarSystem("Luna Sea");
		system.setName("The Luna Sea"); //No "-Star System"

		LocationAPI hyper = Global.getSector().getHyperspace();

		system.addTag(Tags.THEME_HIDDEN);
		system.addTag(Tags.THEME_SPECIAL);
		system.addTag(Tags.THEME_UNSAFE);

		system.setBackgroundTextureFilename("data/strings/com/fs/starfarer/api/impl/campaign/you can hear it cant you/our whispers through the void/our song/graphics/backgrounds/abyss_edf_under.png");
		new AbyssBackgroundWarper(system, 8, 0.25f);

		if (!sector.getDifficulty().equals(Difficulties.EASY)) {
			SectorEntityToken lunasea_nebula = Misc.addNebulaFromPNG("data/strings/com/fs/starfarer/api/impl/campaign/you can hear it cant you/our whispers through the void/our song/graphics/terrain/flower_nebula.png",
					0, 0, // center of nebula
					system, // location to add to
					"terrain", "nebula", // "nebula_blue", // texture to use, uses xxx_map for map
					8, 8, StarAge.AVERAGE); // number of cells in texture
		} else {
			SectorEntityToken lunasea_nebula = Misc.addNebulaFromPNG("data/strings/com/fs/starfarer/api/impl/campaign/you can hear it cant you/our whispers through the void/our song/graphics/terrain/luwunasea_nebula2.png",
					0, 0, // center of nebula
					system, // location to add to
					"terrain", "nebula", // "nebula_blue", // texture to use, uses xxx_map for map
					4, 4, StarAge.AVERAGE); // number of cells in texture
		}

		system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "music_campaign_alpha_site");

		PlanetAPI luna = system.initStar("lunasea_star", StarTypes.BLUE_SUPERGIANT, 2500, 30500, -30500, 0);
		system.setDoNotShowIntelFromThisLocationOnMap(true);

		SectorEntityToken star_corona = system.addTerrain("kol_corona",
				new AbyssCorona.CoronaParams(11000,
						0,
						luna,
						5f,
						0.1f,
						1f)
		);

		//system.generateAnchorIfNeeded();
		JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("abyss_lunasea_jp", "Second Trial");
		//OrbitAPI orbit = Global.getFactory().createCircularOrbit(luna, 90, 10000, 25);
		jumpPoint.setFixedLocation(8500,-1500);
		jumpPoint.setStandardWormholeToHyperspaceVisual();
		system.addEntity(jumpPoint);

		system.autogenerateHyperspaceJumpPoints(false, false); //begone evil clouds
		HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		NebulaEditor editor = new NebulaEditor(plugin);
		float minRadius = plugin.getTileSize() * 1f;

		float radius = system.getMaxRadiusInHyperspace();
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f);
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);

		ElysianSeededFleetManager fleets = new ElysianSeededFleetManager(system, 3, 5, 10, 50, 0.95f);
		system.addScript(fleets);
	}
}