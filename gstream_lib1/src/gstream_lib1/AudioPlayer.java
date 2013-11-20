package gstream_lib1;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.gstreamer.*;
import org.gstreamer.swing.VideoComponent;

public class AudioPlayer {
	
	static JFrame 		frame_main = null;
	static JPanel 		panel_controls = null;
	static JPanel 		panel_progress = null;
	static JPanel 		panel_main = null;
	static JLabel 		lblVolume = null;
	static JLabel 		lblTimeElapsed = null;
	static JLabel 		lblTimeDuration = null;
	static JButton 		btnOpenFile = null;
	static JButton 		btnPlay = null;
	static JButton		btnPause = null;
	static JButton		btnStop = null;
	static JButton 		btnVolumeUp = null;
	static JButton 		btnVolumeDown = null;
	static JButton 		btnSeekForward = null;
	static JButton 		btnSeekBackward = null;
	static JButton 		btnMute = null;
	static JFileChooser chooser = null;;
	static JSlider 		slider = null;
	static KeyAdapter	keyAdapter = null;
	
	static Container 	previousContentPane;
	static boolean 		m_fullScreenActive = false;
	
	static VideoComponent video_frame = null;
	
	static GStreamerClass 	player;
	static boolean 			m_errorDialogRaised = false; 
	static boolean 			m_volumeMuted = false;
	static boolean			m_sliderNotHandle = false;

	/*-------------------------------------------------------------------------------------------------------
	 *  downloaded from: http://stackoverflow.com/questions/15164485/making-a-single-component-full-screen
	 -------------------------------------------------------------------------------------------------------*/
	
	static void goFullScreen() {
        Window w = SwingUtilities.windowForComponent(video_frame);
        if (w instanceof JFrame) {
            JFrame frame = (JFrame) w;
            frame.dispose();
            frame.setUndecorated(true);
            frame.getGraphicsConfiguration().getDevice().setFullScreenWindow(w);
            previousContentPane = frame.getContentPane();
            frame.setContentPane(video_frame);
            frame.revalidate();
            frame.repaint();
            frame.setVisible(true);
            m_fullScreenActive = true;
        }
    }

    static void ungoFullScreen() {
        Window w = SwingUtilities.windowForComponent(video_frame);
        if (w instanceof JFrame) {
            JFrame frame = (JFrame) w;
            frame.dispose();
            frame.setUndecorated(false);
            frame.getGraphicsConfiguration().getDevice().setFullScreenWindow(null);
            frame.setContentPane(previousContentPane);
            panel_main.add(video_frame);
            frame.revalidate();
            frame.repaint();
            frame.setVisible(true);
            m_fullScreenActive = false;
        }
    }
    
    static void addMyListener (Component component) {
    	component.addKeyListener(keyAdapter);
    	if(component instanceof Container) {
    		for ( Component child : ( ( Container ) component ).getComponents () )
            {
                addMyListener(child);
            }
    	}
    }
    
    static void mute() {
    	if(!m_volumeMuted) {
			player.mute();
			lblVolume.setText("MUTED");
			m_volumeMuted = true;
		}
		
		else {
			m_volumeMuted = false;
			lblVolume.setText(String.format("%.0f", player.getVolumeLevel()));
			player.volumeDown();
			player.volumeUp();
		}
    }
    
    static void volumeDown() {
    	player.volumeDown();
		lblVolume.setText(String.format("%.0f", player.getVolumeLevel()));
    }
    
    static void volumeUp() {
    	player.volumeUp();
		lblVolume.setText(String.format("%.0f", player.getVolumeLevel()));
    }
    
    static void forward() {
    	player.seekForward();
		slider.setValue((int)player.getPosition().toSeconds());
    }
    
    static void backward() {
    	player.seekBackward();
		slider.setValue((int)player.getPosition().toSeconds());
    }
	
	public static void main(String[] args) {
		
		args = Gst.init("AudioPlayer", args);
		
		chooser = new JFileChooser();
		chooser.setDialogTitle("Choose a file");
		
		frame_main = new JFrame("Multimedia Player");
		
		video_frame = new VideoComponent();
		
		panel_controls = new JPanel();
		panel_controls.setLayout(new GridLayout(10,1));
		panel_main = new JPanel();
		
		panel_progress = new JPanel();
		panel_progress.setLayout(new BorderLayout());
		
		final TagListener tagListener = new TagListener() {
			
			@Override
			public void tagRecieved(List<String> _tags) {
				if(_tags.size() == 1) {
					frame_main.setTitle(_tags.get(0));
				}
				
				else if(_tags.size() == 2) {
					frame_main.setTitle(_tags.get(0) + " - " + _tags.get(1));
				}
			}
			
		};
		
		final PositionListener positionListener = new PositionListener() {
			
			@Override
			public void positionChanged(ClockTime _position) {
				lblTimeElapsed.setText(_position.toString());
				m_sliderNotHandle = true;
				slider.setValue((int)_position.toSeconds());
			}
		};
		
		final ErrorListener errorListener = new ErrorListener() {
			
			@Override
			public void errorRaised(String _message) {
				JOptionPane.showMessageDialog(frame_main, "This file is not supported");
			}
		};
		
		final EOSListener eosListener = new EOSListener() {
			
			@Override
			public void eosRaised() {
				player.stop();
				slider.setValue(0);
				lblTimeElapsed.setText(player.getPosition().toString());
			}
		};
		
		frame_main.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent arg0) {
				player = new GStreamerClass();
				player.addTagListener(tagListener);
				player.addPositionListener(positionListener);
				player.addErrorListener(errorListener);
				player.addEOSListener(eosListener);
				lblVolume.setText(String.format("%.0f", player.getVolumeLevel()));
				player.setVideoSink(video_frame.getElement());
			}
		});
		
		keyAdapter = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyChar() == 'f') {
					System.out.println("F key pressed");
					if(!m_fullScreenActive) goFullScreen();
					else ungoFullScreen();
				}
				
				else if (e.getKeyChar() == 'm') {
					mute();
				}
				
				else if (e.getKeyCode() == KeyEvent.VK_UP) {
					volumeUp();
				}
				
				else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					volumeDown();
				}
				
				else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					backward();
				}
				
				else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					forward();
				}
			}
		};
		
		btnOpenFile = new JButton("Open FIle");
		btnOpenFile.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				chooser.showOpenDialog(frame_main);
				player.setFile(chooser.getSelectedFile());
				slider.setMinimum(0);
				slider.setMaximum((int) player.getDuration().toSeconds());
				slider.setValue(0);
				lblTimeElapsed.setText(player.getPosition().toString());
				lblTimeDuration.setText(player.getDuration().toString());
			}
		});
		
		btnPlay = new JButton("Play");
		btnPlay.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				player.play();
			}
		});
		
		btnPause = new JButton("Pause");
		btnPause.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				player.pause();
			}
		});
		
		btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				player.stop();
				slider.setValue(0);
				lblTimeElapsed.setText(player.getPosition().toString());
			}
		});

		btnVolumeDown = new JButton("Volume Down");
		btnVolumeDown.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				volumeDown();
			}
		});
		
		btnVolumeUp = new JButton("Volume Up");
		btnVolumeUp.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				volumeUp();
			}
		});
		
		btnSeekForward = new JButton("Forward");
		btnSeekForward.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				forward();
			}
		});
		
		btnSeekBackward = new JButton("Backward");
		btnSeekBackward.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				backward();
			}
		});
		
		btnMute = new JButton("Mute");
		btnMute.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				mute();
			}
		});
		
		video_frame.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2)
				{
					System.out.println("Double click on video");
					goFullScreen();
				}
			}
			
		});
		
		slider = new JSlider();
		slider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				if(!m_sliderNotHandle) {
					player.seekOnTime(slider.getValue());
				}
				
				else m_sliderNotHandle = false;
			}
		});
		
		lblVolume = new JLabel("VOL", SwingConstants.CENTER);
		lblTimeElapsed = new JLabel("Time", SwingConstants.CENTER);
		lblTimeDuration = new JLabel("Time", SwingConstants.CENTER);
		lblTimeElapsed.setPreferredSize(new Dimension(105, 30));
		lblTimeDuration.setPreferredSize(new Dimension(105, 30));
		
		panel_controls.add(btnOpenFile);
		panel_controls.add(btnPlay);
		panel_controls.add(btnPause);
		panel_controls.add(btnStop);
		panel_controls.add(btnSeekForward);
		panel_controls.add(btnSeekBackward);
		panel_controls.add(btnVolumeUp);
		panel_controls.add(lblVolume);
		panel_controls.add(btnVolumeDown);
		panel_controls.add(btnMute);
		
		panel_progress.add(lblTimeElapsed, BorderLayout.WEST);
		panel_progress.add(slider, BorderLayout.CENTER);
		panel_progress.add(lblTimeDuration, BorderLayout.EAST);
		
		BorderLayout layout_main = new BorderLayout();
		
		panel_main.setLayout(layout_main);
		panel_main.add(panel_controls, BorderLayout.WEST);
		panel_main.add(video_frame, BorderLayout.CENTER);
		panel_main.add(panel_progress, BorderLayout.SOUTH);
		
		frame_main.add(panel_main);
		frame_main.setPreferredSize(new Dimension(1000, 600));
		frame_main.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame_main.pack();
		frame_main.setLocationRelativeTo(null);
		frame_main.setVisible(true);

		addMyListener(frame_main);
			
		Gst.main();
	}

}
