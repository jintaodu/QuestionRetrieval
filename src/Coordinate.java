import java.io.Serializable;

public class Coordinate implements Serializable {
	private String x;
	private String y;

	public Coordinate(String x_axis, String y_axis) {
		x = x_axis;
		y = y_axis;
	}

	public boolean equals(Object o) {
		Coordinate co = (Coordinate) o;
		return (x.equals(co.x)&& y.equals(co.y))||(x.equals(co.y)&& y.equals(co.x));
	}

	public int hashCode() {
		return x.hashCode() + y.hashCode();
	}
}