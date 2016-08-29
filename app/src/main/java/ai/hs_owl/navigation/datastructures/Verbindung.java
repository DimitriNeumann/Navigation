package ai.hs_owl.navigation.datastructures;

/**
 * Created by mberg on 25.08.2016.
 */
public class Verbindung {
    int id, idA, idB, gewicht;

    public Verbindung(int id, int idA, int idB, int gewicht) {
        this.id = id;
        this.idA = idA;
        this.idB = idB;
        this.gewicht = gewicht;
    }

    public int getId() {
        return id;
    }

    public int getIdA() {
        return idA;
    }

    public int getIdB() {
        return idB;
    }

    public int getGewicht() {
        return gewicht;
    }
}
