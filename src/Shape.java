import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;

public abstract class Shape {
	
	// Geometria en formato vectorial
	protected Geometry geometry = null;
	
	// Atributos y tags que se anadiran despues
	protected List<ShapeAttribute> attributes;
	
	// Tipo del shape rustico o urbano RU/UR
	protected String tipo = null;
	
	// Id del shape SU TIPO + un numero autoincremental
	protected String shapeId = null;
	
	protected String codigoMasa = null; // Codigo de masa a la que pertenece
	// Esto se usa para la paralelizacion ya que luego solo se simplificaran geometrias que
	// pertenezcan a las mismas masas. Si alguna geometria no tiene codigo de masa, se le
	// asignara el nombre de tipo de archivo
	
	// Fechas de alta y baja en catastro
	protected long fechaAlta; // Fecha de Alta en catastro Formato AAAAMMDD
	protected long fechaBaja; // Fecha de Baja en catastro Formato AAAAMMDD
	
	// Fecha de construccion, iniciada con la fecha de creacion de los archivos de catastro
	protected long fechaConstru = Cat2OsmUtils.getFechaArchivos(); // Formato AAAAMMDD
	

	/**Constructor
	 * @param f Linea del archivo shp
	 */
	public Shape(SimpleFeature f, String tipo){
		
		this.tipo = tipo;
		
		// Algunos conversores de DATUM cambian el formato de double a int en el .shp
		// FECHAALATA y FECHABAJA siempre existen
		if (f.getAttribute("FECHAALTA") instanceof Double){
			double fa = (Double) f.getAttribute("FECHAALTA");
			fechaAlta = (long) fa;
		}
		else if (f.getAttribute("FECHAALTA") instanceof Long){
			fechaAlta = (long) f.getAttribute("FECHAALTA");
		}
		else if (f.getAttribute("FECHAALTA") instanceof Integer){
			int fa = (Integer) f.getAttribute("FECHAALTA");
			fechaAlta = (long) fa;
		}
		else System.out.println("["+new Timestamp(new Date().getTime())+"] No se reconoce el tipo del atributo FECHAALTA "
				+ f.getAttribute("FECHAALTA").getClass().getName());	

		if (f.getAttribute("FECHABAJA") instanceof Integer){
			int fb = (Integer) f.getAttribute("FECHABAJA");
			fechaBaja = (long) fb;
		}
		else  if (f.getAttribute("FECHABAJA") instanceof Double){
			double fb = (Double) f.getAttribute("FECHABAJA");
			fechaBaja = (long) fb;
		}
		else if (f.getAttribute("FECHABAJA") instanceof Long){
			fechaBaja = (long) f.getAttribute("FECHABAJA");
		}
		else System.out.println("["+new Timestamp(new Date().getTime())+"] No se reconoce el tipo del atributo FECHABAJA"
				+ f.getAttribute("FECHABAJA").getClass().getName());
		}

	/** Comprueba la fechaAlta y fechaBaja del shape para ver si se ha creado entre AnyoDesde y AnyoHasta
	 * Deben seguir dados de alta despues de fechaHasta para que los devuelva. Es decir, shapes que se hayan
	 * creado y dado de baja en ese intervalo no las devolvera.
	 * @param fechaDesde fecha a partir de la cual se cogeran los shapes
	 * @param fechaHasta fecha hasta la cual se cogeran
	 * @return boolean Devuelve si se ha creado entre fechaAlta y fechaBaja o no
	 */
	public boolean checkCatastroDate(long fechaDesde, long fechaHasta){
		return (fechaAlta >= fechaDesde && fechaAlta < fechaHasta && fechaBaja >= fechaHasta);
	}
	
	
	/** Comprueba la fecha de construccion del shape para ver si se ha construido entre AnyoDesde y AnyoHasta
	 * @param fechaDesde fecha a partir de la cual se cogeran los shapes
	 * @param fechaHasta fecha hasta la cual se cogeran
	 * @return boolean Devuelve si se ha creado entre fechaAlta y fechaBaja o no
	 */
	public boolean checkBuildingDate(long fechaDesde, long fechaHasta){
		return (fechaConstru >= fechaDesde && fechaConstru <= fechaHasta);
	}


	public long getFechaAlta(){
		return fechaAlta;
	}


	public long getFechaBaja(){
		return fechaBaja;
	}


	public String getTipo(){
		return tipo;
	}
	

	public Geometry getGeometry(){
		return geometry;
	}
	

	public void setGeometry(Geometry geometry){
		this.geometry = geometry;
	}
	
	
	public synchronized long newShapeId(long Id){
		Id++;
		return Id;
	}
	
	
	public void addAttribute(String k, String v){
		
		if(this.attributes == null)
			this.attributes = new ArrayList<ShapeAttribute>();
		
		ShapeAttribute atr = new ShapeAttribute(k, v);
		if (!this.attributes.contains(atr))
			this.attributes.add(atr);
	}
	
	
	public void addAttributesAsStringArray(List<String[]> attributes){
		
		if(this.attributes == null)
			this.attributes = new ArrayList<ShapeAttribute>();
		
		for(String[] str : attributes){
			ShapeAttribute atr = new ShapeAttribute(str[0], str[1]);
			if (!this.attributes.contains(atr))
			this.attributes.add(atr);
		}
	}
	
	public void addAttributes(List<ShapeAttribute> attributes){
		
		if(this.attributes == null)
			this.attributes = new ArrayList<ShapeAttribute>();
		
		for(ShapeAttribute atr : attributes){
			if (!this.attributes.contains(atr))
			this.attributes.add(atr);
		}
	}
	
	
	public String getAttribute(String key){
		for(ShapeAttribute atr : getAttributes())
			if (atr.getKey().equals(key))
				return atr.getValue();
		
		return null;
	}

	
	public List<ShapeAttribute> getAttributes(){
		return attributes;
	}
	
	public long getFechaConstru() {
		return fechaConstru;
	}

	
	public void setFechaConstru(long fechaConstru) {
		if (this.fechaConstru > fechaConstru)
			this.fechaConstru = fechaConstru;
	}
	
	
	public String getShapeId(){
		return shapeId;
	}
	
	
	public String getCodigoMasa(){
		return codigoMasa;
	}
	
	
	public void setCodigoMasa(String cod){
		codigoMasa = cod;
	}
	
	
	public String printAttributes(){
		String s = "";
		
		for(ShapeAttribute attribute : attributes){
			s += attribute.toString();
		}
		return s;
	}
	
	
	public boolean sameAttributes(List<ShapeAttribute> attributes){
		
		if(attributes == null && this.attributes == null)
			return true;
		
		if (attributes == null)
			return false;
		
		if(this.attributes == null)
			return false;
		
		if(this.attributes.size() != attributes.size())
			return false;
		
		List<String> l1 = new ArrayList<String>();
		List<String> l2 = new ArrayList<String>();
		for (ShapeAttribute l : this.attributes)
			l1.add(l.toString());
		Collections.sort(l1);
		for (ShapeAttribute l : attributes)
			l2.add(l.toString());
		Collections.sort(l2);

		return l1.equals(l2);
	}
	
	/////////////////////////////////////////////////////////////////////////
	
	// Metodos abstractos que implementaran sus hijos
	
	public abstract void setNodes(List<List<Long>> nodesId);
	
	public abstract void addNode(int pos, long nodeId);
	
	public abstract List<Long> getNodesIds(int pos);
	
	public abstract void setWays(List<Long> waysId);

	public abstract void addWay(int pos, long wayId);
	
	public abstract List<Long> getWays();
	
	public abstract void deleteWay(long wayId);

	public abstract void setRelation(long relationId);
	
	public abstract Long getRelationId();

	public abstract boolean hasRelevantAttributesInternally();
	
	public abstract boolean hasRelevantAttributesForPrinting();

	public abstract String getRefCat();

	public abstract String getTtggss();

	public abstract boolean isValid();

	//////////////////////////////////////////////////////////////////////////
	
	
	/** Traduce el tipo de via
	 * @param codigo Codigo de via
	 * @return Nombre del tipo de via
	 */
	public static String nombreTipoViaParser(String codigo){

		switch (codigo){
		case "CL":return "Calle";
		case "AG":return "Agregado";
		case "AL":return "Aldea/Alameda";
		case "AR":return "Area/Arrabal";
		case "AU":return "Autopista";
		case "AV":return "Avenida";
		case "AY":return "Arroyo";
		case "BJ":return "Bajada";
		case "BO":return "Barrio";
		case "BR":return "Barranco";
		case "CA":return "Cañada";
		case "CG":return "Colegio/Cigarral";
		case "CH":return "Chalet";
		case "CI":return "Cinturon";
		case "CJ":return "Calleja/Callejón";
		case "CM":return "Camino";
		case "CN":return "Colonia";
		case "CO":return "Concejo/Colegio";
		case "CP":return "Campa/Campo";
		case "CR":return "Carretera/Carrera";
		case "CS":return "Caserío";
		case "CT":return "Cuesta/Costanilla";
		case "CU":return "Conjunto";
		case "DE":return "Detrás";
		case "DP":return "Diputación";
		case "DS":return "Diseminados";
		case "ED":return "Edificios";
		case "EM":return "Extramuros";
		case "EN":return "Entrada, Ensanche";
		case "ER":return "Extrarradio";
		case "ES":return "Escalinata";
		case "EX":return "Explanada";
		case "FC":return "Ferrocarril";
		case "FN":return "Finca";
		case "GL":return "Glorieta";
		case "GR":return "Grupo";
		case "GV":return "Gran Vía";
		case "HT":return "Huerta/Huerto";
		case "JR":return "Jardines";
		case "LD":return "Lado/Ladera";
		case "LG":return "Lugar";
		case "MC":return "Mercado";
		case "ML":return "Muelle";
		case "MN":return "Municipio";
		case "MS":return "Masias";
		case "MT":return "Monte";
		case "MZ":return "Manzana";
		case "PB":return "Poblado";
		case "PD":return "Partida";
		case "PJ":return "Pasaje/Pasadizo";
		case "PL":return "Polígono";
		case "PM":return "Paramo";
		case "PQ":return "Parroquia/Parque";
		case "PR":return "Prolongación/Continuación";
		case "PS":return "Paseo";
		case "PT":return "Puente";
		case "PZ":return "Plaza";
		case "QT":return "Quinta";
		case "RB":return "Rambla";
		case "RC":return "Rincón/Rincona";
		case "RD":return "Ronda";
		case "RM":return "Ramal";
		case "RP":return "Rampa";
		case "RR":return "Riera";
		case "RU":return "Rua";
		case "SA":return "Salida";
		case "SD":return "Senda";
		case "SL":return "Solar";
		case "SN":return "Salón";
		case "SU":return "Subida";
		case "TN":return "Terrenos";
		case "TO":return "Torrente";
		case "TR":return "Travesía";
		case "UR":return "Urbanización";
		case "VR":return "Vereda";
		case "CY":return "Caleya";
		}

		return codigo;
		
//		switch(codigo){
//		case "CL":return "Carrer";
//		case "AG":return "Agregat";
//		case "AL":return "Llogaret";
//		case "AR":return "Raval";
//		case "AU":return "Autopista";
//		case "AV":return "Avinguda";
//		case "AY":return "Rierol";
//		case "BJ":return "Baixada";
//		case "BO":return "Barri";
//		case "BR":return "Barranc";
//		case "CA":return "-";
//		case "CG":return "-";
//		case "CH":return "Xalet";
//		case "CI":return "Cinturó";
//		case "CJ":return "Carreró";
//		case "CM":return "Camí";
//		case "CN":return "Colònia";
//		case "CO":return "-";
//		case "CP":return "Camp";
//		case "CR":return "Carretera";
//		case "CS":return "Mas ??";
//		case "CT":return "Pujada";
//		case "CU":return "Conjunt";
//		case "DE":return "-";
//		case "DP":return "Diputació";
//		case "DS":return "Disseminats";
//		case "ED":return "Edificis";
//		case "EM":return "Extramurs";
//		case "EN":return "Eixample ??";
//		case "ER":return "Extraradi";
//		case "ES":return "Escalinata";
//		case "EX":return "Pla";
//		case "FC":return "Ferrocarril";
//		case "FN":return "Finca";
//		case "GL":return "-";
//		case "GR":return "Grup";
//		case "GV":return "Gran Vía";
//		case "HT":return "Hort";
//		case "JR":return "Jardins";
//		case "LD":return "Vessant ??";
//		case "LG":return "Lloc ??";
//		case "MC":return "Mercat";
//		case "ML":return "Moll";
//		case "MN":return "Municipi";
//		case "MS":return "Masies";
//		case "MT":return "Muntanya ??";
//		case "MZ":return "Illa ??";
//		case "PB":return "Poblat ??";
//		case "PD":return "-";
//		case "PJ":return "Passatge";
//		case "PL":return "Polígon";
//		case "PM":return "-";
//		case "PQ":return "-";
//		case "PR":return "-";
//		case "PS":return "Passeig";
//		case "PT":return "Pont";
//		case "PZ":return "Plaça";
//		case "QT":return "-";
//		case "RB":return "Rambla";
//		case "RC":return "-";
//		case "RD":return "Ronda";
//		case "RM":return "-";
//		case "RP":return "Rampa";
//		case "RR":return "Riera";
//		case "RU":return "Rua";
//		case "SA":return "Sortida";
//		case "SD":return "Sender";
//		case "SL":return "Solar";
//		case "SN":return "-";
//		case "SU":return "Pujada";
//		case "TN":return "Terrenys";
//		case "TO":return "Torrent";
//		case "TR":return "Travessera";
//		case "UR":return "Urbanització";
//		case "VR":return "-";
//		case "CY":return "-";}
		
	}


	/** Traduce el ttggss de Elemlin y Elempun. Elemtex tiene en su clase su propio parser
	 * ya que necesita mas datos suyos propios.
	 * @param ttggss Atributo ttggss
	 * @return Lista de los tags que genera
	 */
	public List<String[]> ttggssParser(String ttggss){
		List<String[]> l = new ArrayList<String[]>();
		String[] s = new String[2];

		switch (ttggss){

		// Divisiones administrativas
		case "010401": 
			s[0] = "admin_level"; s[1] ="2";
			l.add(s);
			s = new String[2];
			s[0] = "boundary"; s[1] ="administrative";
			l.add(s);
			s = new String[2];
			s[0] = "border_type"; s[1] ="nation";
			l.add(s);
			return l;	
		case "010301": 
			s[0] = "admin_level"; s[1] ="4";
			l.add(s);
			s = new String[2];
			s[0] = "boundary"; s[1] ="administrative";
			l.add(s);
			return l;
		case "010201": 
			s[0] = "admin_level"; s[1] ="6";
			l.add(s);
			s = new String[2];
			s[0] = "boundary"; s[1] ="administrative";
			l.add(s);
			return l;
		case "010101": 
			s[0] = "admin_level"; s[1] ="8";
			l.add(s);
			s = new String[2];
			s[0] = "boundary"; s[1] ="administrative";
			l.add(s);
			return l;
		case "010102": 
			s[0] = "admin_level"; s[1] ="10";
			l.add(s);
			s = new String[2];
			s[0] = "boundary"; s[1] ="administrative";
			l.add(s);
			return l;
		case "018507": 
			s[0] = "historic"; s[1] ="boundary_stone";
			l.add(s);
			return l;
		case "018506": 
			s[0] = "historic"; s[1] ="boundary_stone";
			l.add(s);
			return l;

			// Relieve
		case "028110": 
			s[0] = "man_made"; s[1] ="survey_point";
			l.add(s);
			return l;
		case "028112": 
			s[0] = "man_made"; s[1] ="survey_point";
			l.add(s);
			return l;

			// Hidrografia
		case "030102": 
			s[0] = "waterway"; s[1] ="river";
			l.add(s);
			return l;
		case "030202": 
			s[0] = "waterway"; s[1] ="stream";
			l.add(s);
			return l;
		case "030302": 
			s[0] = "waterway"; s[1] ="drain";
			l.add(s);
			return l;
		case "032301": 
			s[0] = "natural"; s[1] ="coastline";
			l.add(s);
			return l;
		case "033301": 
			s[0] = "landuse"; s[1] ="reservoir";
			l.add(s);
			s = new String[2];
			s[0] = "fixme"; s[1] ="Especificar tipo de agua (natural=water / leisure=swimming_pool / man_made=water_well / amenity=fountain / ...), eliminar landuse=reservoir y/o comprobar que no este duplicado o contenido en otra geometria de agua.";
			l.add(s);
			return l;
		case "037101": 
			s[0] = "man_made"; s[1] ="water_well";
			l.add(s);
			return l;
		case "038101": 
			s[0] = "man_made"; s[1] ="water_well";
			l.add(s);
			return l;
		case "038102": 
			s[0] = "amenity"; s[1] ="fountain";
			l.add(s);
			return l;
		case "037102": 
			s[0] = "landuse"; s[1] ="reservoir";
			l.add(s);
			s = new String[2];
			s[0] = "fixme"; s[1] ="Especificar tipo de agua (natural=water / leisure=swimming_pool / man_made=water_well / amenity=fountain / ...), eliminar landuse=reservoir y/o comprobar que no este duplicado o contenido en otra geometria de agua.";
			l.add(s);
			return l;
		case "037107": 
			s[0] = "waterway"; s[1] ="dam";
			l.add(s);
			return l;

			// Vias de comunicacion
		case "060102": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "060104": 
			s[0] = "highway"; s[1] ="motorway";
			l.add(s);
			return l;
		case "060202": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "060204": 
			s[0] = "highway"; s[1] ="primary";
			l.add(s);
			return l;
		case "060402": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "060404": 
			s[0] = "highway"; s[1] ="track";
			l.add(s);
			return l;
		case "060109": 
			s[0] = "railway"; s[1] ="funicular";
			l.add(s);
			return l;
		case "061104": 
			s[0] = "railway"; s[1] ="rail";
			l.add(s);
			return l;
		case "067121": 
			s[0] = "bridge"; s[1] ="yes";
			l.add(s);
			return l;
		case "068401": 
			s[0] = "highway"; s[1] ="milestone";
			l.add(s);
			return l;

			// Red geodesica y topografica
		case "108100": 
			s[0] = "man_made"; s[1] ="survey_point";
			l.add(s);
			return l;
		case "108101": 
			s[0] = "man_made"; s[1] ="survey_point";
			l.add(s);
			return l;
		case "108104": 
			s[0] = "man_made"; s[1] ="survey_point";
			l.add(s);
			return l;

			// Delimitaciones catastrales urbanisticas y estadisticas
		case "111101": 
			s[0] = "admin_level"; s[1] ="10";
			l.add(s);
			s = new String[2];
			s[0] = "boundary"; s[1] ="administrative";
			l.add(s);
			return l;
		case "111000": 
			s[0] = "admin_level"; s[1] ="12";
			l.add(s);
			return l;
		case "111200": 
			s[0] = "admin_level"; s[1] ="14";
			l.add(s);
			return l;
		case "111300": 
			s[0] = "admin_level"; s[1] ="10";
			l.add(s);
			return l;
		case "115101": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "115000": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "115200": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "115300": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;

			// Rustica (Compatibilidad 2006 hacia atras)
		case "120100": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "120200": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "120500": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "120180": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "120280": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "120580": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "125101": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "125201": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "125501": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "125510": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;

			// Rustica y Urbana
		case "130100": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "130200": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "130500": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "135101": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "135201": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "135501": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "135510": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;

			// Urbana (Compatibilidad 2006 hacia atras)
		case "140100": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "140190": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "140200": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "140290": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "140500": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "140590": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "145101": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "145201": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "145501": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "145510": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;

			// Infraestructura/Mobiliario
		case "160101": 
			s[0] = "kerb"; s[1] ="yes";
			l.add(s);
			return l;
		case "160131": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "160132": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "160201": 
			s[0] = "power"; s[1] ="line";
			l.add(s);
			return l;
		case "160202": 
			s[0] = "telephone"; s[1] ="line";
			l.add(s);
			return l;
		case "160300": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "161101": 
			s[0] = "highway"; s[1] ="road";
			l.add(s);
			return l;
		case "167103": 
			s[0] = "historic"; s[1] ="monument";
			l.add(s);
			return l;
		case "167104": 
			s[0] = "highway"; s[1] ="steps";
			l.add(s);
			return l;
		case "167106": 
			s[0] = "highway"; s[1] ="footway";
			l.add(s);
			s = new String[2];
			s[0] = "tunnel"; s[1] ="yes";
			l.add(s);
			return l;
		case "167111": 
			s[0] = "power"; s[1] ="sub_station";
			l.add(s);
			return l;
		case "167167": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "167201": 
			s[0] = "barrier"; s[1] ="hedge";
			l.add(s);
			return l;
		case "168100": 
			s[0] = "ttggss"; s[1] =ttggss;
			l.add(s);
			return l;
		case "168103": 
			s[0] = "historic"; s[1] ="monument";
			l.add(s);
			return l;
		case "168113": 
			s[0] = "power"; s[1] ="pole";
			l.add(s);
			return l;
		case "168116": 
			s[0] = "highway"; s[1] ="street_lamp";
			l.add(s);
			return l;
		case "168153": 
			s[0] = "natural"; s[1] ="tree";
			l.add(s);
			return l;
		case "168168": 
			s[0] = "amenity"; s[1] ="parking_entrance";
			l.add(s);
			return l;
		default: if (!ttggss.isEmpty()){
			s[0] = "fixme"; s[1] = "Documentar ttggss="+ttggss+" si es preciso en http://wiki.openstreetmap.org/w/index.php?title=Traduccion_metadatos_catastro_a_map_features";
			l.add(s);
		}
		}
		return l;
	}


	/** Elimina los puntos '.' y espacios en un String
	 * @param s String en el cual eliminar los puntos
	 * @return String sin los puntos
	 */
	public static String eliminarPuntosString(String s){
		if (!s.isEmpty()){
			s = s.replace('.', ' ');
			s = s.replace(" ", "");
		}
		return s.trim();
	}


	/** Eliminar las comillas '"' de los textos, sino al leerlo JOSM devuelve error
	 * pensando que ha terminado un valor antes de tiempo.
	 * @param s String al que quitar las comillas
	 * @return String sin las comillas
	 */
	public static String eliminarComillas(String s){
		String ret = "";
		for (int x = 0; x < s.length(); x++)
			if (s.charAt(x) != '"') ret += s.charAt(x);
		return ret;
	}

}
