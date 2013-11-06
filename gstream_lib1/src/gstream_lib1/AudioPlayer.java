package gstream_lib1;

import java.awt.Dimension;
import java.awt.GridLayout;

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
	static String artist = "";
	static JFileChooser chooser;

	public static void main(String[] args) {
		args = Gst.init("AudioPlayer", args);

		chooser = new JFileChooser();
		frame = new JFrame("File Chooser");
		panel = new JPanel();
		
		GridLayout layout = new GridLayout(2, 1);
		panel.setLayout(layout);
		
		label_artist = new JLabel("Label", SwingConstants.CENTER);
		label_song = new JLabel("Label2", SwingConstants.CENTER);

		frame.add(panel);
		
		panel.add(label_artist);
		panel.add(label_song);
		
		frame.setPreferredSize(new Dimension(200, 100));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		chooser.showOpenDialog(frame);

		PlayBin2 play = new PlayBin2("bin");
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

		play.setState(State.PLAYING);
		Gst.main();
		play.setState(State.NULL);
	}

}
