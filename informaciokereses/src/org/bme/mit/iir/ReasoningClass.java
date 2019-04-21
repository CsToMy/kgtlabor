package org.bme.mit.iir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerRuntimeException;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public class ReasoningClass {

	public static final String UNI_ONTOLOGY_FNAME = "data\\family2.owl";
	
	public static final String UNI_BASE_URI = 
		"http://www.semanticweb.org/tomi/ontologies/family";
    
	public static final IRI ANNOTATION_TYPE_IRI =
            OWLRDFVocabulary.RDFS_LABEL.getIRI();

    OWLOntologyManager manager;
    OWLOntology ontology;
    OWLReasoner reasoner;
    OWLDataFactory factory;
    
    public ReasoningClass(String ontologyFilename) {
        // Hozzunk létre egy OntologyManager példányt. Többek között ez tartja 
    	// nyilván, hogy mely ontológiákat nyitottuk meg és azok hogyan 
    	// hivatkoznak egymásra. 
        manager = OWLManager.createOWLOntologyManager();

        // Töltsük be az ontológiát egy fizikai címről.
        // [Az ontológiáknak külön van logikai és fizikai címe, a kettő közötti 
        // leképezést az URIMapper-ek határozzák meg: manager.addURIMapper(...).
        // Erre ontológiák közötti függőségek (import) kialakításánál van 
        // szükség.]
        ontology = null;
		try {
			ontology = manager.loadOntologyFromOntologyDocument(
					new File(ontologyFilename));
		} catch (Exception e) {
			System.err.println("Hiba az ontológia betöltése közben:\n\t"
					+ e.getMessage());
			System.exit(-1);
		}
        System.out.println("Ontológia betöltve: " + 
                manager.getOntologyDocumentIRI(ontology));

        // Létrehozzuk a következtetőgép egy példányát. Mi most a HermiT-et 
        // használjuk, de az OWL API univerzális, másik következtető 
        // használatához csak a Factory osztály nevét kellene kicserélni.
        
        OWLReasonerFactory reasonerFactory = new org.semanticweb.HermiT.Reasoner.ReasonerFactory();
        //
        // Hozzunk létre egy következtetőgépet, betöltve az ontológiát.
        // [Ha vannak az ontológiának függőségei, azok automatikusan
        // betöltődnek a manager segítségével.]
        reasoner = reasonerFactory.createReasoner(ontology);

        try {
	        // Ha az ontológia nem konzisztens, lépjünk ki.
	        if (!reasoner.isConsistent()) {
	        	System.err.println("Az ontológia nem konzisztens!");
	        	
	            Node<OWLClass> incClss = reasoner.getUnsatisfiableClasses();
                System.err.println("A következő osztályok nem konzisztensek: "
                    + Util.join(incClss.getEntities(), ", ") + ".");
	        	System.exit(-1);
	        } else
	        	System.out.println("Az ontológia konzisztens!");
		} catch (OWLReasonerRuntimeException e) {
			System.err.println("Hiba a következtetőben: " + e.getMessage());
			System.exit(-1);
		}

        // Példányosítsuk az OWLDataFactory-t, aminek a segítségével létre tudunk
        // hozni URI alapján OWL entitásokat (osztályt, tulajdonságot, stb).
        factory = manager.getOWLDataFactory();
    }
    
    /**
    // Lekérdezi egy osztály leszármazottait a PC-Shop ontológiában.
    //   className: a keresett osztály neve
    // Az OWL következtető Set<Set<..>> struktúrákban, azaz halmazok 
    // halmazaként adja vissza a lekérdezés eredményét. A belső halmazok 
    // egymással ekvivalens, jelentés szempontjából megegyező dolgokat 
    // tartalmaznak. Például egy lekérdezés eredménye:
    // { { Kutya, Eb }, { Macska, Cica }, { Ló } }
    // Ez a függvény a fenti halmazt "kilapítva" adja vissza:
    // { Kutya, Eb, Macska, Cica, Ló }
     * 
     * @param className
     * @param direct
     * @return
     */
    public Set<OWLClass> getSubClasses(String className, boolean direct) {
    	// Létrehozzuk az osztály URI-ját a base URI alapján.
        IRI clsIRI = IRI.create(UNI_BASE_URI + className);
        // Ellenőrizzük, hogy az osztály szerepel-e az ontológiában.
        // (Ha olyan dologra kérdezünk rá a következtetővel, ami nem szerepel az 
        // ontológiában, az nem vezet hibához az OWL nyílt világ feltételezése 
        // miatt.)
        if (!ontology.containsClassInSignature(clsIRI)) {
        	System.out.println("Nincs ilyen osztály az ontológiában: \"" + 
        			className + "\"");
        	return Collections.emptySet();
        }
        // Létrehozzuk az osztály egy példányát és lekérdezzük a leszármazottait.
        OWLClass cls = factory.getOWLClass(clsIRI);
        NodeSet<OWLClass> subClss;
		try {
			subClss = reasoner.getSubClasses(cls, direct);
		} catch (OWLReasonerRuntimeException e) {
			System.err.println("Hiba az alosztályok következtetése közben: "
					+ e.getMessage());
			return Collections.emptySet();
		}
		// Kiírjuk az eredményt, az ekvivalens osztályokat 
		// egyenlőségjellel elválasztva.
        System.out.println("Az \"" + className + "\" osztály leszármazottai:");
        for (Node<OWLClass> subCls : subClss.getNodes()) {
        	System.out.println("  - " + Util.join(subCls.getEntities(), " = "));
        }
        return subClss.getFlattened();
    }

    /**
    // Lekérdezi egy OWL entitás (osztály, tulajdonság vagy egyed) annotációit
    // az ontológiából. (Ehhez nincs szükség a következtetőre.)
    // Az annotációknak sok fajtája van, a lekérdezett típust az
    // ANNOTATION_TYPE_IRI konstans tartalmazza, jelen esetben rdfs:label.
     * 
     * @param entity
     * @return
     */
    public Set<String> getClassAnnotations(OWLEntity entity) {
        OWLAnnotationProperty label =
                factory.getOWLAnnotationProperty(ANNOTATION_TYPE_IRI);
        Set<String> result = new HashSet<String>();
        for (OWLAnnotation a : entity.getAnnotations(ontology, label)) {
            if (a.getValue() instanceof OWLLiteral) {
                OWLLiteral value = (OWLLiteral)a.getValue();
                result.add(value.getLiteral());
            }
        }
        return Collections.unmodifiableSet(result);
    }
    
    public Set<String> getInvidiuals(String indvClass) {
    	return null;
    }
    
    public void createInvidTargy(String name, String class_, String data, int value) {
    	IRI classIRI = IRI.create(UNI_BASE_URI + class_);
    	IRI invidualIRI = IRI.create(UNI_BASE_URI + name);
    	IRI dataIRI = IRI.create(UNI_BASE_URI + data);
    	
    	OWLIndividual targy = factory.getOWLNamedIndividual(invidualIRI);
    	OWLClass tantargy = factory.getOWLClass(classIRI);
    	OWLClassAssertionAxiom targyAx = factory.getOWLClassAssertionAxiom(tantargy, targy);
    	
    	OWLDataPropertyExpression kredit = factory.getOWLDataProperty(dataIRI);
    	OWLDataPropertyAssertionAxiom kreditAx = factory.getOWLDataPropertyAssertionAxiom(kredit, targy, value);
    	
    	manager.addAxiom(ontology, kreditAx);
    	manager.addAxiom(ontology, targyAx);
    	
    	File fileout = new File(UNI_ONTOLOGY_FNAME);    	
    	try {
			manager.saveOntology(ontology, new FileOutputStream(fileout));
		} catch (OWLOntologyStorageException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}/**/
    	
    }
}