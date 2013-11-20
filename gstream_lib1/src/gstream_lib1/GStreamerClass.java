package gstream_lib1;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

import javax.swing.Timer;

import org.gstreamer.Bus;
import org.gstreamer.ClockTime;
import org.gstreamer.Element;
import org.gstreamer.Format;
import org.gstreamer.GstObject;
import org.gstreamer.TagList;
import org.gstreamer.media.PlayBinMediaPlayer;

public class GStreamerClass {

	private List<TagListener> 		m_tagListeners = new ArrayList<TagListener>();
	private List<PositionListener> 	m_positionListeners = new ArrayList<PositionListener>();
	private List<ErrorListener>		m_errorListeners = new ArrayList<ErrorListener>();
	private List<EOSListener> 		m_eosListeners = new ArrayList<EOSListener>();
	
	PlayBinMediaPlayer 	m_play = null;
	double 				m_volumeLevel;
	ClockTime			m_duration;
	Timer 				m_timer = null;
	ClockTime 			m_position;
	
	
	public double getVolumeLevel() {
		return m_volumeLevel;
	}
	
	public ClockTime getDuration() {
		return m_duration;
	}
	
	public ClockTime getPosition() {
		return m_position;
	}

	public GStreamerClass() {
		m_play = new PlayBinMediaPlayer("bin", null);
		m_volumeLevel = 5;
		m_position = ClockTime.fromSeconds(0);
		
		m_timer = new Timer(1000, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				long position;
				
				while((position = m_play.getPipeline().queryPosition(Format.TIME)) == -1)
				{
					//
				}
				
				m_position = ClockTime.fromNanos(position);
				for(PositionListener listener : m_positionListeners) {
					listener.positionChanged(m_position);
				}
			}
		});
		
		m_play.getPipeline().getBus().connect(new Bus.TAG() {

			public void tagsFound(GstObject source, TagList tagList) {
				
				List<String> returnTagList = new ArrayList<String>();
				
				for (String tagName : tagList.getTagNames()) {
					for (final Object tagData : tagList.getValues(tagName)) {
						
						if (tagName.equals("artist")) {
							returnTagList.add(tagData.toString());
						}
						
						else if (tagName.equals("title")) {
							returnTagList.add(tagData.toString());
						}
					}
				}
				
				for(TagListener listener : m_tagListeners) {
					listener.tagRecieved(returnTagList);
				}
			}
		});
		
		m_play.getPipeline().getBus().connect(new Bus.ERROR() {
			
			@Override
			public void errorMessage(GstObject arg0, int arg1, String arg2) {
				//
			}
		});
		
		m_play.getPipeline().getBus().connect(new Bus.WARNING() {
			
			@Override
			public void warningMessage(GstObject arg0, int arg1, String arg2) {
				System.out.println("Warning");
			}
		});
		
		m_play.getPipeline().getBus().connect(new Bus.EOS() {
			
			@Override
			public void endOfStream(GstObject arg0) {
				m_position = ClockTime.fromSeconds(0);
				for(EOSListener listener : m_eosListeners) {
					listener.eosRaised();
				}
			}
		});
	}
	
	public void addTagListener(TagListener _listener) {
		m_tagListeners.add(_listener);
	}
	
	public void removeTagListener(TagListener _listener) {
		m_tagListeners.remove(_listener);
	}
	
	public void addPositionListener(PositionListener _listener) {
		m_positionListeners.add(_listener);
	}
	
	public void removePositionListener(PositionListener _listener) {
		m_positionListeners.remove(_listener);
	}
	
	public void addErrorListener(ErrorListener _listener) {
		m_errorListeners.add(_listener);
	}
	
	public void removeErrorListener(ErrorListener _listener) {
		m_errorListeners.remove(_listener);
	}
	
	public void addEOSListener(EOSListener _listener) {
		m_eosListeners.add(_listener);
	}
	
	public void removeEOSListener(EOSListener _listener) {
		m_eosListeners.remove(_listener);
	}
	
	public void setFile(File _file) {
		m_play.setInputFile(_file);
		m_play.play();
		m_play.play();
		m_play.pause();
		m_duration = m_play.getPipeline().queryDuration();
	}
	
	public void setVideoSink(Element _sink) {
		m_play.setVideoSink(_sink);
	}
	
	public void play() {
		m_play.play();
		m_timer.start();
	}
	
	public void pause() {
		m_play.pause();
		m_timer.stop();
	}
	
	public void stop() {
		m_play.stop();
		m_timer.stop();
		m_position = ClockTime.fromSeconds(0);
	}
	
	public void seekForward() {
		long seekTime = m_position.toSeconds() + 30;
		m_play.getPipeline().seek(ClockTime.fromSeconds(seekTime));
		m_position = ClockTime.fromSeconds(seekTime);
	}
	
	public void seekBackward() {
		long seekTime = m_position.toSeconds() - 30;
		m_play.getPipeline().seek(ClockTime.fromSeconds(seekTime));
		m_position = ClockTime.fromSeconds(seekTime);
	}
	
	public void seekOnTime(long _seconds) {
		m_play.getPipeline().seek(ClockTime.fromSeconds(_seconds));
	}
	
	public void volumeUp() {
		m_volumeLevel += 1;
		if(m_volumeLevel > 10) m_volumeLevel = 10;
		m_play.setVolume(m_volumeLevel/10);
	}
	
	public void volumeDown() {
		m_volumeLevel -= 1;
		if(m_volumeLevel < 0) m_volumeLevel = 0;
		m_play.setVolume(m_volumeLevel/10);
	}
	
	public void mute() {
		m_play.setVolume(0);
	}
}
