package org.selkie.kol.world;

import java.util.ArrayList;
import java.util.Arrays;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import exerelin.campaign.SectorManager;
import org.selkie.kol.plugins.KOL_ModPlugin;

public class GenerateKnights {
	static StarSystemAPI Canaan = Global.getSector().getStarSystem("Canaan");
	static StarSystemAPI Kumari = Global.getSector().getStarSystem("Kumari Kandam");
	static StarSystemAPI Eos = Global.getSector().getStarSystem("Eos Exodus");
	static StarSystemAPI Gebbar = Global.getSector().getStarSystem("Al Gebbar");
	
	public static void zugg() {
		Eos.setBackgroundTextureFilename("graphics/backgrounds/kol_bg_1.jpg");
		Kumari.setBackgroundTextureFilename("graphics/backgrounds/kol_bg_2.jpg");
		Canaan.setBackgroundTextureFilename("graphics/backgrounds/kol_bg_3.jpg");
		Gebbar.setBackgroundTextureFilename("graphics/backgrounds/kol_bg_4.jpg");
		genKnightsBattlestation();
		genKnightsStarfortress();
	}
	
	public static void genKnightsBattlestation() {
        SectorEntityToken cygnus = Canaan.addCustomEntity("kol_cygnus", "Battlestation Cygnus", "station_lowtech2", "knights_of_selkie");
        cygnus.setCircularOrbitPointingDown(Canaan.getEntityById("canaan_gate"), 33, 275, 99);
        cygnus.setCustomDescriptionId("kol_cygnus_desc");
        
        addMarketplace("knights_of_selkie", cygnus, null, "Battlestation Cygnus", 4,
        		new ArrayList<String>(Arrays.asList(Conditions.OUTPOST,
                        Conditions.POPULATION_4)),
        		new ArrayList<String>(Arrays.asList(
                        Industries.POPULATION,
                        Industries.SPACEPORT,
						"kol_garden",
                        Industries.LIGHTINDUSTRY,
                        Industries.GROUNDDEFENSES,
                        Industries.BATTLESTATION)),
        		new ArrayList<String>(Arrays.asList(
        				Submarkets.SUBMARKET_STORAGE,
                        Submarkets.GENERIC_MILITARY,
                        Submarkets.SUBMARKET_BLACK,
                        Submarkets.SUBMARKET_OPEN)),
        		0.3f
        );

		if (KOL_ModPlugin.haveNex) SectorManager.NO_BLACK_MARKET.add("kol_cygnus");
	}
	
	public static void genKnightsStarfortress() {
        SectorEntityToken lyra = Eos.addCustomEntity("kol_lyra", "Star Keep Lyra", "station_lowtech3", "knights_of_selkie");
        lyra.setCircularOrbitPointingDown(Eos.getEntityById("eos_exodus_gate"), 33, 275, 99);
        lyra.setCustomDescriptionId("kol_lyra_desc");
        
        addMarketplace("knights_of_selkie", lyra, null, "Star Keep Lyra", 5,
        		new ArrayList<String>(Arrays.asList(Conditions.OUTPOST,
                        Conditions.POPULATION_5)),
        		new ArrayList<String>(Arrays.asList(
                        Industries.POPULATION,
                        Industries.SPACEPORT,
						"kol_garden",
                        Industries.MILITARYBASE,
                        Industries.ORBITALWORKS,
                        Industries.FUELPROD,
                        Industries.HEAVYBATTERIES,
                        Industries.STARFORTRESS,
                        Industries.WAYSTATION)),
        		new ArrayList<String>(Arrays.asList(
        				Submarkets.SUBMARKET_STORAGE,
                        Submarkets.GENERIC_MILITARY,
                        Submarkets.SUBMARKET_BLACK,
                        Submarkets.SUBMARKET_OPEN)),
        		0.3f
        );



		if (KOL_ModPlugin.haveNex) SectorManager.NO_BLACK_MARKET.add("kol_lyra");
	}
	
	public static MarketAPI addMarketplace(String factionID, SectorEntityToken primaryEntity, ArrayList<SectorEntityToken> connectedEntities, String name, 
            int size, ArrayList<String> marketConditions, ArrayList<String> Industries, ArrayList<String> submarkets, float tariff, boolean hidden) {
    	EconomyAPI globalEconomy = Global.getSector().getEconomy();  
    	String planetID = primaryEntity.getId();  
    	String marketID = planetID;

    	MarketAPI newMarket = Global.getFactory().createMarket(marketID, name, size);  
    	newMarket.setFactionId(factionID);  
    	newMarket.setPrimaryEntity(primaryEntity);  
    	newMarket.getTariff().modifyFlat("generator", tariff);  

    	if (null != submarkets){  
    		for (String market : submarkets){  
    			newMarket.addSubmarket(market);  
    		}  
    	}  

    	for (String condition : marketConditions) {  
    		newMarket.addCondition(condition);  
    	}

    	for (String industry : Industries) {
    		newMarket.addIndustry(industry);
    	}

    	if (null != connectedEntities) {  
    		for (SectorEntityToken entity : connectedEntities) {  
    			newMarket.getConnectedEntities().add(entity);  
    		}  
    	}  

    	globalEconomy.addMarket(newMarket, true);  
    	primaryEntity.setMarket(newMarket);
    	primaryEntity.setFaction(factionID);

    	if (null != connectedEntities) {  
    		for (SectorEntityToken entity : connectedEntities) {  
    			entity.setMarket(newMarket);
    			entity.setFaction(factionID);
    		}
    	}
    	
    	if (!hidden) newMarket.setSurveyLevel(SurveyLevel.FULL);

    	return newMarket;
	}
	
    public static MarketAPI addMarketplace(String factionID, SectorEntityToken primaryEntity, ArrayList<SectorEntityToken> connectedEntities, String name, 
            int size, ArrayList<String> marketConditions, ArrayList<String> Industries, ArrayList<String> submarkets, float tariff) {
    	return addMarketplace(factionID, primaryEntity, connectedEntities, name, size, marketConditions, Industries, submarkets, tariff, false);
    }
}
