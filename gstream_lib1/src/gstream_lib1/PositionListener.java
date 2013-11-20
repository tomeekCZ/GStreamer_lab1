package gstream_lib1;

import org.gstreamer.ClockTime;

public interface PositionListener {
	public void positionChanged(ClockTime _position);
}
