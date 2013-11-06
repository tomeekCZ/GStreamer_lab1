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

public class AudioPlayer {

	static JFrame frame = null;
	static JPanel panel = null;
	static JLabel label_artist = null;
	static JLabel label_song = null;
	
	static JButton btnOpenFile = null;
	static JButton btnPlaySong = null;
	static JButton btnStopSong = null;
	
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
		
		GridLayout layout = new GridLayout(5, 1);
		panel.setLayout(layout);
		
		label_artist = new JLabel("Label", SwingConstants.CENTER);
		label_song = new JLabel("Label2", SwingConstants.CENTER);

		frame.add(panel);
		
		panel.add(btnOpenFile);
		panel.add(btnPlaySong);
		panel.add(btnStopSong);
		panel.add(label_artist);
		panel.add(label_song);
		
		frame.setPreferredSize(new Dimension(200, 200));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		

		Gst.main();
		play.setState(State.NULL);
	}

}
