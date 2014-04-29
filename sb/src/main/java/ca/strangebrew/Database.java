package ca.strangebrew;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 * $Id: Database.java,v 1.27 2012/06/03 17:07:52 dougedey Exp $
 * @author aavis
 *
 * This is the Database class that reads in the .csv files and 
 * creates ArrayLists of ingredient objects.  It uses the 
 * csv reader from the com.mindprod.csv package.
 * 
 * TODO: create methods to add and delete items, detect if the
 * list had changed, and save the DB to csv.
 */
public class Database {

	// this class is just some lists of various ingredients
	// read from the csv files.

	private static Database instance = null;
	private Options preferences = Options.getInstance();
	public List<Fermentable> fermDB = new ArrayList<Fermentable>();
	public List<Fermentable> stockFermDB = new ArrayList<Fermentable>();
	public List<Hop> hopsDB = new ArrayList<Hop>();
	public List<Hop> stockHopsDB = new ArrayList<Hop>();
	final public List<Yeast> yeastDB = new ArrayList<Yeast>();
	public List<Style> styleDB = new ArrayList<Style>();
	final public List<Misc> miscDB = new ArrayList<Misc>();
	final public List<PrimeSugar> primeSugarDB = new ArrayList<PrimeSugar>();
	final public List<WaterProfile> waterDB = new ArrayList<WaterProfile>();
	public String dbPath;
	private String styleFileName;
	private SQLiteDatabase conn = null;

	// This is now a singleton
	private Database() throws UnsupportedEncodingException {
		
		dbPath = Environment.getExternalStorageDirectory() + "/StrangeBrew/Data";
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			Debug.print("Couldn't find the SQLIte JDBC Driver");
			//return;
		}
        try {
        	
        	
        	Debug.print("Trying to open database: "+ dbPath);
			conn = SQLiteDatabase.openDatabase(
                    dbPath + File.separator + "sb_ingredients.db", null, 0);
		
			
			Debug.print("Checking for tables");
            ArrayList<String> tables = new ArrayList<String>();
            Cursor c = conn.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

            if (c.moveToFirst()) {
                while ( !c.isAfterLast() ) {
                    tables.add( c.getString( c.getColumnIndex("name")) );
                    c.moveToNext();
                }
            }

			if (!tables.contains("styleguide")) {
				// no Style guide
				Debug.print("Creating styleguide table");
				conn.execSQL("create table styleguide (Item INT AUTO_INCREMENT,Name, Category, OG_Low, OG_High, Alc_Low, Alc_High, IBU_Low, IBU_High, Lov_Low, Lov_High," +
                        "Appearance, Aroma, Flavor, Mouthfeel, Impression, Comments, Ingredients, Comm_examples, Year );");
			}
			
			if (!tables.contains("fermentables")) {
				// no fermentables
				Debug.print("Creating fermentables table");
                conn.execSQL("create table fermentables (Item INT AUTO_INCREMENT,Name UNIQUE,Yield,Lov,Cost,Stock,Units,Mash,Descr,Steep,Modified, Ferments );");
			} else {
				Debug.print("Checking for fermentables updates");
				
				// This will cover us if we need to add new rows in the future, to enable people to update the DB
				Map<String, String> newColumns = new HashMap<String, String>();
				
				newColumns.put("Ferments", "True");
				
				// Iterate the Map
				Iterator <Entry<String, String>> it = newColumns.entrySet().iterator();
				
				while (it.hasNext()) {
                    Entry<String, String> e = it.next();
                    try {
                        conn.execSQL("ALTER TABLE fermentables ADD " + e.getKey() + " DEFAULT " + e.getValue());
                    } catch (SQLException columnExists) {
                        // Ignore it
                    }
                }
			}
			
			if (!tables.contains("hops")) {
				// no hops
				Debug.print("Creating hops table");
                conn.execSQL("create table hops (Item INT AUTO_INCREMENT,Name,Alpha,Cost,Stock,Units,Descr,Storage,Date,Modified,Type);");
			} else {
				
				// Add a new column to store the type
				Cursor testColumns = conn.rawQuery("SELECT * FROM hops LIMIT 1;", null);



                if (testColumns.getColumnIndex("Type") >= 0) {
                    conn.execSQL("ALTER TABLE hops ADD Type");
				}
			}
	        
			if(!tables.contains("misc")) {
				// no misc
				Debug.print("Creating misc table");
                conn.execSQL("create table misc (Item INT AUTO_INCREMENT,Name,Descr,Stock,Units,Cost,Stage,Modified);");
			}
			
			if(!tables.contains("prime")) {
				// no prime
				Debug.print("Creating prime table");
                conn.execSQL("create table prime  ( Item INT AUTO_INCREMENT,Name,Yield,Units,Descr);");
			}
			
			if(!tables.contains("yeast")) {
				// no yeast
				Debug.print("Creating yeast table");
                conn.execSQL("create table yeast (Item INT AUTO_INCREMENT,Name,Cost,Descr,Modified);");
			}
			if(!tables.contains("water_profiles")) {
				// no water_profiles 
				Debug.print("Creating water_profiles table");
                conn.execSQL("create table water_profiles ( Item INT AUTO_INCREMENT, Name,Descr,Ca,Mg,Na,SO4,HCO3,Cl,Hardness,TDS,PH,Alk);");
			}

			
	        Debug.print("Created Database succesfully!");
        } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static Database getInstance() {
		if (instance == null) {
			try {
				instance = new Database();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return instance;
	}
	
	public int inDB(Object o){		
		if (o instanceof Yeast) {
			for (int i=0; i<yeastDB.size(); i++){
				Yeast y1 = (Yeast)o;
				Yeast y2 = (Yeast)yeastDB.get(i);
				if (y1.equals(y2)) {					
					return i;			
				}
			}			
		} else if (o instanceof Fermentable) {
			for (int i=0; i<fermDB.size(); i++){
				Fermentable y1 = (Fermentable)o;
				Fermentable y2 = (Fermentable)fermDB.get(i);
				if (y1.equals(y2)) {					
					return i;			
				}
			}
		} else if (o instanceof Hop) {
			for (int i=0; i<hopsDB.size(); i++){
				Hop y1 = (Hop)o;
				Hop y2 = (Hop)hopsDB.get(i);
				if (y1.equals(y2)) {					
					return i;			
				}
			}
		} else if (o instanceof Misc) {
			for (int i=0; i<miscDB.size(); i++){
				Misc y1 = (Misc)o;
				Misc y2 = (Misc)miscDB.get(i);
				if (y1.equals(y2)) {					
					return i;			
				}
			}
		} else if (o instanceof Style) {
			for (int i=0; i<styleDB.size(); i++){
				Style y1 = (Style)o;
				Style y2 = (Style)styleDB.get(i);
				if (y1.equals(y2)) {					
					return i;			
				}
			}
		} else if (o instanceof PrimeSugar) {
			for (int i=0; i<primeSugarDB.size(); i++){
				PrimeSugar y1 = (PrimeSugar)o;
				PrimeSugar y2 = (PrimeSugar)primeSugarDB.get(i);
				if (y1.equals(y2)) {					
					return i;			
				}
			}
		} else if (o instanceof WaterProfile) {
			for (int i=0; i<waterDB.size(); i++){
				WaterProfile y1 = (WaterProfile)o;
				WaterProfile y2 = (WaterProfile)waterDB.get(i);
				if (y1.equals(y2)) {					
					return i;			
				}
			}
		}
		
		return -1;
	}
	
	public void readDB(String path, String styleYear){
		
		// check to see if the style guide is in the DB already
		
		dbPath = path;

		try {
			styleDB.clear();
			stockFermDB.clear();
			fermDB.clear();
			stockHopsDB.clear();
			hopsDB.clear();
			yeastDB.clear();
			waterDB.clear();

            Debug.print("Loading Fermentables");
			readFermentables(dbPath);
            Debug.print("Loading Sugar");
			readPrimeSugar(dbPath);
            Debug.print("Loading Hops");
			readHops(dbPath);
            Debug.print("Loading Yeast");
			readYeast(dbPath);
            Debug.print("Loading Misc");
			readMisc(dbPath);
            Debug.print("Loading Styles");
			readStyles(dbPath, styleYear);
            Debug.print("Loading Water");
			readWater(dbPath);
		
			
			// sort
			Debug.print(styleDB.size());
			Collections.sort(styleDB);
			Collections.sort(fermDB);
			Collections.sort(hopsDB);
			Collections.sort(yeastDB);
			Collections.sort(waterDB);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	public void readFermentables(String path) {
		// read the fermentables from the csv file
		// get the current date just because
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date date = new Date();
		
		try {

			Fermentable f = new Fermentable();
			Cursor res = conn.rawQuery("SELECT * FROM fermentables", null);
			// we weren't clearing this when updating
			stockFermDB.clear();
			
			res.moveToFirst();

            int name = res.getColumnIndex("Name");
            int yield = res.getColumnIndex("Yield");
            int lov = res.getColumnIndex("Lov");
            int cost = res.getColumnIndex("Cost");
            int units = res.getColumnIndex("Units");
            int mash = res.getColumnIndex("Mash");
            int steep = res.getColumnIndex("Steep");
            int ferments = res.getColumnIndex("Ferments");
            int stock = res.getColumnIndex("Stock");
            int descr = res.getColumnIndex("Descr");
            int modified = res.getColumnIndex("Modified");

			while (!res.isAfterLast()) {
				f = new Fermentable();
				//Item,Name,Yield,Lov,Cost,Stock,Units,Mash,Descr,Steep,Modified 
				
				f.setName(res.getString(name));
				f.setPppg(Double.parseDouble(res.getString(yield)));
				f.setLov(Double.parseDouble(res.getString(lov)));
				f.setCost(Double.parseDouble(res.getString(cost)));
				f.setUnits(res.getString(units));
				f.setMashed(Boolean.valueOf(res.getString(mash)));
				f.setSteep(Boolean.valueOf(res.getString(steep)));
				f.ferments(Boolean.valueOf(res.getString(ferments)));

                String stockStr = res.getString(stock);
				if (stockStr != null && stockStr.equals("")) {
					f.setStock(Double.parseDouble(res.getString(stock)));
				}
				else
					f.setStock(0);
				
				
				f.setDescription(res.getString(descr));
				f.setModified(Boolean.valueOf(res.getString(modified)));
				fermDB.add(f);
				
				// check to see if we have nonStock set
				
				if(f.getStock() > 0.00 ) {
					Debug.print("Adding to Stock DB");
					stockFermDB.add(f); 
				}
                res.moveToNext();
			}
			Debug.print("Ferm Found " + res.getCount());
			
			Collections.sort(fermDB);

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.exit(-1);
		} catch (Exception e) {
            e.printStackTrace();
        }

	}

	public void writeFermentables() {		
		// Don't need to do this anymore, we store as soon as we add anything to the DB!
		Debug.print("Saving fermentables to file");
		
		try {
			String pStatement = "SELECT COUNT(*) FROM fermentables WHERE name = ?;";
			
			String rStatement = "SELECT COUNT(*) FROM fermentables " +
					"WHERE name = ? AND Yield =? AND Lov=? AND Cost=? AND Stock=? AND" +
					" Units=? AND Mash=? AND Descr=? AND Steep=? AND Ferments=?;";
			
			
			String insertFerm = "insert into fermentables (Name,Yield,Lov,Cost,Stock,Units,Mash,Descr,Steep, Ferments) " +
					"values(?, ?,  ? , ?, ?, ?, ?, ?, ?, ? )";
			

			
			String updateFerm = "UPDATE fermentables SET Yield =?, Lov=?, Cost=?, Stock=?," +
					" Units=?, Mash=?, Descr=?, Steep=?, Ferments=? " +
					" WHERE name = ?";
			

			Cursor res = null;
	
			int i = 0;
			
		
			// sort the list first
			Collections.sort(fermDB);
			
			while (i < fermDB.size()) {
				Fermentable f = fermDB.get(i);
				i++;

				res = conn.rawQuery(pStatement,
                        new String[] {f.getName()});
				res.moveToFirst();
                String[] qargs = {
                        f.getName(),
                        Double.toString(f.getPppg()),
                        Double.toString(f.getLov()),
                        Double.toString(f.getCostPerU()),
                        Double.toString(f.getStock()),
                        f.getUnitsAbrv(),
                        Boolean.toString(f.getMashed()),
                        f.getDescription(),
                        Boolean.toString(f.getSteep()),
                        Boolean.toString(f.ferments())
                };

				if(res.getInt(1) == 0){

					conn.execSQL(insertFerm, qargs);
				} else {
					res.close();
					res = conn.rawQuery(rStatement, qargs);
					
					// we have a name match, see if anything has changed
					if(res.getInt(1) == 0) {
						res.close();
						Debug.print("Fermentables Update");
                        String[] uargs = {
                                Double.toString(f.getPppg()),
                                Double.toString(f.getLov()),
                                Double.toString(f.getCostPerU()),
                                Double.toString(f.getStock()),
                                f.getUnitsAbrv(),
                                Boolean.toString(f.getMashed()),
                                f.getDescription(),
                                Boolean.toString(f.getSteep()),
                                Boolean.toString(f.ferments()),
                                f.getName()
                        };
						conn.execSQL(updateFerm, uargs);
						
					}
				}
			}
			//clear the list
			fermDB = new ArrayList<Fermentable>();
			stockFermDB = new ArrayList<Fermentable>();
			
			Debug.print("Trying to update DB at: "+dbPath);
			readFermentables(dbPath);
			
			
			//this.readFermentables(dbPath);
			

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void readHops(String path) {
		
		// Open the database and save the hops in
		// get the current date just because
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date date = new Date();
		Cursor res = null;

        res = conn.rawQuery("SELECT * FROM hops", null);

        hopsDB.clear();

        res.moveToFirst();

        int nameIdx = res.getColumnIndex("Name");
        int alphaIdx = res.getColumnIndex("Alpha");
        int costIdx = res.getColumnIndex("Cost");
        int stockIdx = res.getColumnIndex("Stock");
        int unitsIdx = res.getColumnIndex("Units");
        int descrIdx = res.getColumnIndex("Descr");
        int storeIdx = res.getColumnIndex("Storage");
        int dateIdx = res.getColumnIndex("Date");
        int modIdx = res.getColumnIndex("Modified");
        int typeIdx = res.getColumnIndex("Type");

		while (!res.isAfterLast()) {
			Hop h = new Hop();
			//Item,Name,Alpha,Cost,Stock,Units,Descr,Storage,Date,Modified
			h.setName(res.getString(nameIdx));
			h.setAlpha(Double.parseDouble(res.getString(alphaIdx)));
			
			if (!res.getString(costIdx).equals(""))
				h.setCost(Double.parseDouble(res.getString(costIdx)));
			
			h.setUnits(res.getString(unitsIdx));
			String t = res.getString(stockIdx);
			if (t != null && !t.equals(""))
				h.setStock(Double.parseDouble(t));
			
			h.setDescription(res.getString(descrIdx));

            t = res.getString(storeIdx);
			if (t != null && !t.equals(""))
				h.setStorage(Double.parseDouble(t));
			
			h.setDate(res.getString(dateIdx));
			
			h.setModified(Boolean.valueOf(res.getString(modIdx)).booleanValue());
			
			{ // test for the type
				String tempType = res.getString(typeIdx);
				if (tempType == null || tempType.equalsIgnoreCase("false")) {
					tempType = preferences.getProperty("optHopsType");
				}
				
				h.setType(tempType);
			}
			
			hopsDB.add(h);
			
			if (h.getStock() > 0) {
				stockHopsDB.add(h); 
			} 
			
			//Item,Name,Yield,Lov,Cost,Stock,Units,Mash,Descr,Steep,Modified 

            res.moveToNext();
		}
		Debug.print("Hops Found " + res.getCount());
		Collections.sort(hopsDB);
		

	}
	
	public void writeHops() {		
			// Name,Alpha,Cost,Stock,Units,Descr,Storage,Date,Modified
		Debug.print("Write Hops to DB");
		try {
			String pStatement = "SELECT COUNT(*) FROM hops WHERE name = ?;";
			String rStatement = "SELECT COUNT(*) FROM hops WHERE name = ?" +
					" AND Alpha=? AND Cost=? AND Stock=? AND " +
					"Units=? AND Descr=? AND Storage=? AND Type = ?;";
			
			String insertHop = "insert into hops (Name,Alpha,Cost,Stock,Units,Descr,Storage,Date, Type) " +
					"values(?, ?,  ? , ?, ?, ?, ?, ?, ? )";

            String updateHop = "UPDATE hops SET Alpha=?,Cost=?,Stock=?," +
					"Units=?,Descr=?,Storage=?,Date=?,Type=? " +
					"WHERE name = ?";
			

			Cursor res = null;
		
			int i = 0;
			
			//Sort the list
			Collections.sort(hopsDB);
			
			while (i < hopsDB.size()) {
				Hop h = hopsDB.get(i);
				i++;
				res = conn.rawQuery(pStatement, new String[] {h.getName()});

                res.moveToFirst();

				// Does this hop exist?
				if(res.getInt(1) == 0){
                    // Nope, insert it
					String iargs[] = {
                        h.getName(),
                        Double.toString(h.getAlpha()),
					    Double.toString(h.getCostPerU()),
					    Double.toString(h.getStock()),
					    h.getUnitsAbrv(),
					    h.getDescription(),
					    Double.toString(h.getStorage()),
					    h.getDate().toString(),
					    h.getType()};
			
					conn.execSQL(insertHop, iargs);
				} else { // check to see if we need to update this hop
					
					String iargs[] = {
                        h.getName(),
					    Double.toString(h.getAlpha()),
					    Double.toString(h.getCostPerU()),
					    Double.toString(h.getStock()),
					    h.getUnitsAbrv(),
					    h.getDescription(),
					    Double.toString(h.getStorage()),
					    h.getType()};
					

					res = conn.rawQuery(rStatement, iargs);
					
					res.moveToFirst();
				
					if(res.getInt(1) == 0) {
						// update required
                        String uargs[] = {
                            h.getName(),
                            Double.toString(h.getAlpha()),
                            Double.toString(h.getCostPerU()),
                            Double.toString(h.getStock()),
                            h.getUnitsAbrv(),
                            h.getDescription(),
                            Double.toString(h.getStorage()),
                            h.getDate().toString(),
                            h.getType()};

                        conn.execSQL(updateHop, uargs);
					}
					
				}
			}

			//clear the list
			hopsDB = new ArrayList<Hop>();
			stockHopsDB = new ArrayList<Hop>();
			
			readHops(dbPath);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	public void readYeast(String path) {
        // load from the database
        Cursor res = null;

        try {
            res = conn.rawQuery("SELECT * FROM yeast", null);

            yeastDB.clear();

            res.moveToFirst();

            int nameIdx = res.getColumnIndex("Name");
            int costIdx = res.getColumnIndex("Cost");
            int descrIdx = res.getColumnIndex("Descr");
            int modIdx = res.getColumnIndex("Modified");

            while (!res.isAfterLast()) {
                Yeast y = new Yeast();

                y.setName(res.getString(nameIdx));

                String t = res.getString(costIdx);
                if (t != null && !t.equals(""))
                    y.setCost(Double.parseDouble(t));

                y.setDescription(res.getString(descrIdx));
                y.setModified(Boolean.valueOf(res.getString(modIdx)));
                yeastDB.add(y);
                res.moveToNext();
            }

        Debug.print("Yeast" + res.getCount());

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Collections.sort(hopsDB);
        Collections.sort(stockHopsDB);
 	}
	
	public void writeYeast() {		
		
		//No need to do this now
		Debug.print("Write yeast called");
		
		
		try {
			String pStatement = "SELECT COUNT(*) FROM Yeast WHERE name = ?;";
			
			String insertYeast = "insert into yeast (Name,Cost,Descr) " +
					"values(?, ?,  ?  )";

			Cursor res = null;
		
			
			// Sort the Yeast
			Collections.sort(yeastDB);
			int i = 0;
			while (i < yeastDB.size()) {
				//Item,Name,Cost,Descr,Modified
				Yeast y = yeastDB.get(i);
				i++;
				res = conn.rawQuery(pStatement, new String[] {y.getName()});
				res.moveToFirst();
				if(res.getInt(1) == 0) {
					
					conn.execSQL(insertYeast, new String[] {
                        y.getName(), Double.toString(y.getCostPerU()), y.getDescription()});

				}
			}

			

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	}

	public void readStyles(String path, String year){
		// ead the styles from the csv file
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date date = new Date();
		Cursor res = null;

		try {
			res = conn.rawQuery("SELECT * FROM styleguide WHERE Year = ?", new String[] {year});

			styleDB.clear();

            int nameIdx = res.getColumnIndex("Name");
            int catIdx = res.getColumnIndex("Category");
            int oglowIdx = res.getColumnIndex("OG_Low");
            int oghighIdx = res.getColumnIndex("OG_High");
            int alclowIdx = res.getColumnIndex("Alc_Low");
            int alchighIdx = res.getColumnIndex("Alc_High");
            int ibulowIdx = res.getColumnIndex("IBU_Low");
            int ibuhighIdx = res.getColumnIndex("IBU_High");
            int lovlowIdx = res.getColumnIndex("Lov_Low");
            int lovhighIdx = res.getColumnIndex("Lov_High");
            int commexIdx = res.getColumnIndex("Comm_Examples");
            int yearIdx = res.getColumnIndex("Year");
            int descrIdx = res.getColumnIndex("Descr");
            int appearIdx = res.getColumnIndex("Appearance");
            int aromaIdx = res.getColumnIndex("Aroma");
            int flavourIdx = res.getColumnIndex("Flavour");
            int mouthfeelIdx = res.getColumnIndex("Mouthfeel");
            int impressionIdx = res.getColumnIndex("Impression");
            int commentsIdx = res.getColumnIndex("Comments");
            int ingrIdx = res.getColumnIndex("Ingredients");

            res.moveToFirst();
			while (!res.isAfterLast()) {
                Style s = new Style();
                //Item,Name, Category,OG_Low,OG_High,Alc_Low,Alc_High,IBU_Low,IBU_High,Lov_Low,Lov_High,Comm_examples,Descr
                s.setName(res.getString(nameIdx));
                s.setCategory(res.getString(catIdx));
                s.setOgLow(Double.parseDouble(res.getString(oglowIdx)));
                s.setOgHigh(Double.parseDouble(res.getString(oghighIdx)));
                s.setAlcLow(Double.parseDouble(res.getString(alclowIdx)));
                s.setAlcHigh(Double.parseDouble(res.getString(alchighIdx)));
                s.setIbuLow(Double.parseDouble(res.getString(ibulowIdx)));
                s.setIbuHigh(Double.parseDouble(res.getString(ibuhighIdx)));
                s.setSrmLow(Double.parseDouble(res.getString(lovlowIdx)));
                s.setSrmHigh(Double.parseDouble(res.getString(lovhighIdx)));

                s.setYear(res.getString(yearIdx));

                if(commexIdx > -1 && res.getString(commexIdx) != null)
                    s.setExamples(res.getString(commexIdx));

                if(appearIdx > -1 && res.getString(appearIdx) != null)
                    s.appearance = res.getString(appearIdx);
                if(aromaIdx > -1 && res.getString(aromaIdx) != null)
                    s.aroma = res.getString(aromaIdx);
                if(flavourIdx > -1 && res.getString(flavourIdx) != null)
                    s.flavour = res.getString(flavourIdx);
                if(mouthfeelIdx > -1 && res.getString(mouthfeelIdx) != null)
                    s.mouthfeel = res.getString(mouthfeelIdx);
                if(impressionIdx > -1 && res.getString(impressionIdx) != null)
                    s.impression = res.getString(impressionIdx);
                if(commentsIdx > -1 && res.getString(commentsIdx) != null)
                    s.comments = res.getString(commentsIdx);
                if(ingrIdx > -1 && res.getString(ingrIdx) != null)
                    s.ingredients = res.getString(ingrIdx);
                //		Debug.print("Adding style " + s.getName() + s.toText()


                s.setComplete();
                styleDB.add(s);
                res.moveToNext();
			}
			
			Debug.print("Style Found " + res.getCount());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
//	public void importStyles(String path, String year){
//
//		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
//		Date date = new Date();
//
//		Cursor res = null;
//
//		File styleFile = null;
//
//		try {
//			String select = "SELECT * FROM styleguide WHERE year = ?";
//
//
//			res = conn.rawQuery(select, new String[] {year});
//			//Item,Name, Category, OG_Low, OG_High, IBU_Low, IBU_High, Lov_Low, Lov_High,"
//			//Appearance, Aroma, Flavor, Mouthfeel, Impression, Comments, Ingredients, Year
//
//            res.moveToFirst();
//			while(!res.isAfterLast()) {
//				Style s = new Style();
//
//				s.setName(res.getString("Name"));
//				s.setCategory(res.getString("Category"));
//				s.setOgHigh(Double.parseDouble(res.getString("OG_High")));
//				s.setOgLow(Double.parseDouble(res.getString("OG_Low")));
//				s.setAlcLow(Double.parseDouble(res.getString("Alc_Low")));
//				s.setAlcHigh(Double.parseDouble(res.getString("Alc_High")));
//				s.setIbuLow(Double.parseDouble(res.getString("IBU_Low")));
//				s.setIbuHigh(Double.parseDouble(res.getString("IBU_High")));
//				s.setSrmLow(Double.parseDouble(res.getString("Lov_Low")));
//				s.setSrmHigh(Double.parseDouble(res.getString("Lov_High")));
//				if(res.getString("Comm_examples") != null)
//					s.setExamples(res.getString("Comm_examples"));
//				if(res.getString("Appearance") != null)
//					s.appearance = res.getString("Appearance");
//				if(res.getString("Aroma") != null)
//					s.aroma = res.getString("Aroma");
//				if(res.getString("Flavor") != null)
//					s.flavour = res.getString("Flavor");
//				if(res.getString("Mouthfeel") != null)
//					s.mouthfeel = res.getString("Mouthfeel");
//				if(res.getString("Impression") != null)
//					s.impression = res.getString("Impression");
//				if(res.getString("Comments") != null)
//					s.comments = res.getString("Comments");
//				if(res.getString("Ingredients") != null)
//					s.ingredients = res.getString("Ingredients");
//		//		Debug.print("Adding style " + s.getName() + s.toText());
//				s.setComplete();
//				styleDB.add(s);
//			}
//
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NullPointerException e) {
//			e.printStackTrace();
//		}
//		Debug.print("Loaded Styles");
//
//
//	}
	
	public void readMisc(String path) {

		// Get the fields from the database
		try {
			Debug.print("Loading misc ingredients");
			String pStatement = "SELECT * FROM misc;";
			
			miscDB.clear();

			Cursor res = conn.rawQuery(pStatement, null);
			// get the first line and set up the index:
            res.moveToFirst();
            String[] fields = res.getColumnNames();

            int nameIdx = getIndex(fields, "NAME");
            int costIdx = getIndex(fields, "COST");
            int descrIdx = getIndex(fields, "DESCR");
            int unitsIdx = getIndex(fields, "UNITS");
            int stockIdx = getIndex(fields, "STOCK");
            int stageIdx = getIndex(fields, "STAGE");

			while (!res.isAfterLast()) {
				Misc m = new Misc();
				
				m.setName(res.getString(nameIdx));

                String t = res.getString(costIdx);
				if ((t != null) && !t.equals(""))
					m.setCost(Double.parseDouble(t));
				
				m.setDescription(res.getString(descrIdx));
				m.setUnits(res.getString(unitsIdx));
				m.setStage(res.getString(stageIdx));

				miscDB.add(m);
                res.moveToNext();
			}
			Debug.print("Found " + res.getCount());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		

	}
	
	public void writeMisc() {		
		Debug.print("Write misc called");
		try {
			String select = "SELECT COUNT(*) FROM misc WHERE name = ?;";
			
			String insert = "insert into misc (Name,Descr,Stock,Units,Cost,Stage) " +
					"values(?, ?,  ?,  ? ,  ?,?  )";

			Cursor res = null;
		
		
			int i = 0;
			
			while (i < miscDB.size()) {
				Misc m = miscDB.get(i);
				res = conn.rawQuery(select, new String[] {m.getName()});
				
				res.moveToFirst();
				if(res.getInt(1) == 0) {
					//Name,Descr,Stock,Units,Cost,Stage
				
					String[] iargs = {
                        m.getName(),
                        Double.toString(m.getCostPerU()),
                        m.getDescription(),
					    m.getUnitsAbrv(),
					    m.getStage(),
					    Double.toString(m.getCostPerU())
                    };
					
					conn.execSQL(insert, iargs);
				
				}
				
				i++;
			}



		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	public void readPrimeSugar(String path) {
		// read the fermentables from the csv file


		Cursor res;
		Debug.print("Opening Priming Sugar");

		try {
			res = conn.rawQuery("SELECT * FROM prime;", null);
			
			primeSugarDB.clear();
			// loop the results
            res.moveToFirst();
            String[] fields = res.getColumnNames();
            int nameIdx = getIndex(fields, "Name");
            int yieldIdx = getIndex(fields, "Yield");
            int unitsIdx = getIndex(fields, "Units");
            int descrIdx = getIndex(fields, "Descr");

			while(!res.isAfterLast()) {
				PrimeSugar p = new PrimeSugar();
				
				p.setName(res.getString(nameIdx));
				p.setYield(Double.parseDouble(res.getString(yieldIdx)));
				p.setUnits(res.getString(unitsIdx));
				p.setAmount(0);
				p.setDescription(res.getString(descrIdx));
				primeSugarDB.add(p);
                res.moveToNext();
			}
			Debug.print("Prime Found " + res.getCount());
			
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void readWater(String path) {

		Cursor res = null;
		Debug.print("Reading water");

		try {
			// Now read the Database out
			
			res = conn.rawQuery("SELECT * FROM water_profiles;", null);

			waterDB.clear();

            res.moveToFirst();
            // get the first line and set up the index:
            String[] fields = res.getColumnNames();
            int nameIdx = getIndex(fields, "NAME");
            int descrIdx = getIndex(fields, "DESCR");
            int caIdx = getIndex(fields, "Ca");
            int mgIdx = getIndex(fields, "Mg");
            int naIdx = getIndex(fields, "Na");
            int so4Idx = getIndex(fields, "SO4");
            int hco3Idx = getIndex(fields, "HCO3");
            int clIdx = getIndex(fields, "Cl");
            int hardnessIdx = getIndex(fields, "Hardness");
            int tdsIdx = getIndex(fields, "TDS");
            int phIdx = getIndex(fields, "PH");
            int alkIdx = getIndex(fields, "Alk");

			while(!res.isAfterLast() ) {
				
				//Name,Descr,Ca,Mg,Na,SO4,HCO3,Cl,Hardness,TDS,PH,Alk
				WaterProfile w = new WaterProfile();
				w.setName(res.getString(nameIdx));
				w.setDescription(res.getString(descrIdx));
				w.setCa(Double.parseDouble(res.getString(caIdx)));
				w.setMg(Double.parseDouble(res.getString(mgIdx)));
				w.setNa(Double.parseDouble(res.getString(naIdx)));
				w.setSo4(Double.parseDouble(res.getString(so4Idx)));
				w.setHco3(Double.parseDouble(res.getString(hco3Idx)));
				w.setCl(Double.parseDouble(res.getString(clIdx)));
				w.setHardness(Double.parseDouble(res.getString(hardnessIdx)));
				w.setTds(Double.parseDouble(res.getString(tdsIdx)));
				w.setPh(Double.parseDouble(res.getString(phIdx)));
				w.setAlkalinity(Double.parseDouble(res.getString(alkIdx)));
								
				waterDB.add(w);
                res.moveToNext();
			}
			
			Debug.print("Water Found " + res.getCount());
		}catch (SQLException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		Debug.print("Read all water");
	}	
	
	public String[] getPrimeSugarNameList() {
		String[] names = new String[primeSugarDB.size()];
		
		for (int i = 0; i < primeSugarDB.size(); i++) {
			names[i] = ((PrimeSugar)primeSugarDB.get(i)).getName();
		}
	
		return names;
	}
	
	private int getIndex(String[] fields, String key) {
		int i = 0;
		while (i < fields.length && !fields[i].equalsIgnoreCase(key)) {
			i++;
		}
		if (i >= fields.length) // wasn't found
			return -1;
		else
			return i;
	}
	
	public int find(Object seek) {
		// check the objects and seek specifically
		int index = -1;
		if(seek instanceof Fermentable) {
			index = Collections.binarySearch(fermDB, (Fermentable)seek);
		} else if(seek instanceof Hop) {
			Comparator<Hop> c = new Comparator<Hop>()  {
				public int compare(Hop h1, Hop h2){
					
					int result = h1.getName().compareToIgnoreCase(h2.getName());
						
					return result ;
				}
			
			};
			index = Collections.binarySearch(hopsDB, (Hop)seek, c);
		} else if(seek instanceof Yeast) {
			index = Collections.binarySearch(yeastDB, (Yeast)seek);
		}
		// we can't find the object, lets return
		return index;
	}
	
	public void backupFile(File in){
		try {
		File out = new File (in.getAbsolutePath() + ".bak");
		FileChannel sourceChannel = new FileInputStream(in).getChannel();
		FileChannel destinationChannel = new FileOutputStream(out).getChannel();
		sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
		// or
		//  destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
		sourceChannel.close();
		destinationChannel.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public boolean loadRRecipes() {

		// Reads the list of recipes on the remote Database

	    try
	    {
	
	    	String baseURL = Options.getInstance().getProperty("cloudURL");
	    	URI rURI = new URI("http", null, baseURL, 80, "/recipes/", null, null);
	    	Debug.print("Trying to access: " + rURI.toString());
	    	URL url = rURI.toURL();
	    	InputStream response = url.openStream();
	    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

	        dbf.setValidating(false);
	        dbf.setIgnoringComments(false);
	        dbf.setIgnoringElementContentWhitespace(true);
	        dbf.setNamespaceAware(true);
	        // dbf.setCoalescing(true);
	        // dbf.setExpandEntityReferences(true);

	        DocumentBuilder db = null;
	        db = dbf.newDocumentBuilder();
	        //db.setEntityResolver(new NullResolver());

	        // db.setErrorHandler( new MyErrorHandler());

	        Document readXML = db.parse(response);
	        
	        
	        NodeList childNodes = readXML.getChildNodes();
	        
	        Debug.print("Found: " + childNodes.getLength() + "Recipes " + childNodes.item(0).getNodeName());
	        // check that we have a valid first node recipe
	        if(childNodes.item(0).getNodeName().equals("recipes")){
	        	return true;
	        }
	        
	        Debug.print("False");
	        return false;
	        /*
	        for(int x = 0; x < childNodes.getLength(); x++ ) {
	        	
	        	
	        	Node child = childNodes.item(x);
		        
	        	NamedNodeMap childAttr = child.getAttributes();
	        	
	        	childAttr.getNamedItem("id");
	        	
	        	
		    
	        }*/
	        
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	return false;
	    }
	    

	}
} 