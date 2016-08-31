
public class Weg implements Comparable<Weg> {

	int ortA;

	int ortB;

	int weglaenge;

	@Override

	public int compareTo(Weg o) {

		if (this.ortA != o.ortA) {

			return o.ortA - this.ortA;

		}

		if (this.ortB != o.ortB) {

			return o.ortB - this.ortB;

		}

		return o.weglaenge - this.weglaenge;

	}
}