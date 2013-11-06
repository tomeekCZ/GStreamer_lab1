package gstream_lib1;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.gstreamer.*;
import org.gstreamer.elements.*;

import sun.io.Converters;
import sun.misc.JavaLangAccess;

public class AudioPlayer {

	static JFrame frame = null;
	static JPanel panel = null;
	static JLabel label_artist = null;
	static JLabel label_song = null;
	static JLabel volume_label = null;
	
	static JButton btnOpenFile = null;
	static JButton btnPlaySong = null;
	static JButton btnStopSong = null;
	static JButton btnVolumeUp = null;
	static JButton btnVolumeDown = null;
	
	static double volume_level = 0.5;
	
	static String artist = "";
	static JFileChooser chooser;
	
	static PlayBin2 play = null;

	public static void main(String[] args) {
		args = Gst.init("AudioPlayer", args);
		
		chooser = new JFileChooser();
		frame = new JFrame("File Chooser");
		panel = new JPanel();
		
		btnOpenFile = new JButton("Open FIle");
		btnOpenFile.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				chooser.showOpenDialog(frame);
				play = null;
				play = new PlayBin2("bin");
				play.setInputFile(chooser.getSelectedFile());
				play.setVideoSink(ElementFactory.make("fakesink", "sink"));
				
				play.getBus().connect(new Bus.TAG() {

					public void tagsFound(GstObject source, TagList tagList) {
						for (String tagName : tagList.getTagNames()) {
							for (final Object tagData : tagList.getValues(tagName)) {
								System.out.printf("[%s]=%s\n", tagName, tagData);
								if (tagName.equals("artist")) {
									label_artist.setText(tagData.toString());
								}
								
								else if (tagName.equals("title")) {
									label_song.setText(tagData.toString());
								}
							}
						}
					}
				});
			}
		});
		
		btnPlaySong = new JButton("Play");
		btnPlaySong.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				play.setState(State.PLAYING);
			}
		});
		
		btnStopSong = new JButton("Stop");
		btnStopSong.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				play.setState(State.PAUSED);
			}
		});

		btnVolumeDown = new JButton("Volume Down");
		btnVolumeDown.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				volume_level -= 0.1;
				play.set("volume", volume_level);
				volume_label.setText(String.valueOf(volume_level));
			}
		});
		
		btnVolumeUp = new JButton("Volume Up");
		btnVolumeUp.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				volume_level += 0.1;
				play.set("volume", volume_level);
				volume_label.setText(String.valueOf(volume_level));
			}
		});
		
		GridLayout layout = new GridLayout(8, 1);
		panel.setLayout(layout);
		
		label_artist = new JLabel("Artist", SwingConstants.CENTER);
		label_song = new JLabel("Title", SwingConstants.CENTER);
		volume_label = new JLabel(String.valueOf(volume_level), SwingConstants.CENTER);

		frame.add(panel);
		
		panel.add(btnOpenFile);
		panel.add(btnPlaySong);
		panel.add(btnStopSong);
		panel.add(label_artist);
		panel.add(label_song);
		panel.add(btnVolumeUp);
		panel.add(btnVolumeDown);
		panel.add(volume_label);
		
		frame.setPreferredSize(new Dimension(200, 200));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		

		Gst.main();
		play.setState(State.NULL);
	}

}
