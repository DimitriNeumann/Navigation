package ai.hs_owl.navigation.datastructures;

/**
 * Stellt eine einfache Datenstruktur für einen Knoten dar.
 */
public class Knoten {
    private int id,x,y,ebene;
    private String beschreibung;

    public Knoten(int id, int x, int y, int ebene, String beschreibung) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.ebene = ebene;
        this.beschreibung = beschreibung;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getEbene() {
        return ebene;
    }

    public String getBeschreibung() {
        return beschreibung;
    }
}
