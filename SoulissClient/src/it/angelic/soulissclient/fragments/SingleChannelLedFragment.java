package it.angelic.soulissclient.fragments;

import static junit.framework.Assert.assertTrue;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.typicals.SoulissTypical;
import it.angelic.soulissclient.model.typicals.SoulissTypical16AdvancedRGB;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.pheelicks.visualizer.VisualizerView;
import com.pheelicks.visualizer.renderer.BarGraphRenderer;

public class SingleChannelLedFragment extends AbstractMusicVisualizerFragment {
	private SoulissDBHelper datasource = new SoulissDBHelper(SoulissClient.getAppContext());
	private SoulissPreferenceHelper opzioni;

	private Button buttPlus;
	private Button buttMinus;

	private Button btOff;
	private Button btOn;
	private SoulissTypical16AdvancedRGB collected;
	// private SoulissTypical related;

	private Button btWhite;
	private Button btFlash;
	private Button btSleep;

	private int color = 0;
	// Color change listener.
	private OnColorChangedListener dialogColorChangedListener = null;
	private VisualizerView mVisualizerView;
	// private CheckBox checkMusic;

	private boolean continueIncrementing;
	// private Runnable senderThread;
	private boolean continueDecrementing;
	private ToggleButton togMulticast;
	private TableRow tableRowVis;
	private TableRow tableRowChannel;
	private Spinner modeSpinner;
	private SeekBar seekChannelRed;
	private SeekBar seekChannelGreen;
	private SeekBar seekChannelBlue;
	private TextView redChanabel;
	private TextView eqText;
	private TextView blueChanabel;
	private TextView greenChanabel;
	private Button btEqualizer;
	private TableRow tableRowEq;

	/**
	 * Serve per poter tenuto il bottone brightness
	 * 
	 * @param cmd
	 */
	private void startIncrementing(final Short cmd) {
		setIsIncrementing(true);
		new Thread(new Runnable() {
			public void run() {
				while (isIncrementing()) {
					issueIrCommand(cmd, Color.red(color), Color.green(color), Color.blue(color),
							togMulticast.isChecked());
				}
			}
		}).start();
	}

	synchronized private void stopIncrementing() {
		setIsIncrementing(false);
	}

	synchronized private boolean isIncrementing() {
		return continueIncrementing;
	}

	/**
	 * Serve per poter tenuto il bottone brightness
	 * 
	 * @param cmd
	 */
	private void startDecrementing(final Short cmd) {
		setIsDecrementing(true);
		new Thread(new Runnable() {
			public void run() {
				while (isDecrementing()) {
					issueIrCommand(cmd, Color.red(color), Color.green(color), Color.blue(color),
							togMulticast.isChecked());
				}
			}
		}).start();
	}

	synchronized private void stopDecrementing() {
		setIsDecrementing(false);
	}

	/**
	 * Per gestire tasto premuto
	 * 
	 * @param newSetting
	 */
	synchronized void setIsIncrementing(boolean newSetting) {
		continueIncrementing = newSetting;
	}

	synchronized private boolean isDecrementing() {
		return continueDecrementing;
	}

	synchronized void setIsDecrementing(boolean newSetting) {
		continueDecrementing = newSetting;
	}

	/**
	 * Interface describing a color change listener.
	 */
	public interface OnColorChangedListener {
		/**
		 * Method colorChanged is called when a new color is selected.
		 * 
		 * @param color
		 *            new color.
		 */
		void colorChanged(int color);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		opzioni = SoulissClient.getOpzioni();
		// tema
		if (opzioni.isLightThemeSelected())
			getActivity().setTheme(R.style.LightThemeSelector);
		else
			getActivity().setTheme(R.style.DarkThemeSelector);
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if (!opzioni.isDbConfigured()) {
			AlertDialogHelper.dbNotInitedDialog(getActivity());
		}

	}

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null)
			return null;
		opzioni = SoulissClient.getOpzioni();
		View ret = inflater.inflate(R.layout.frag_rgb_advanced, container, false);
		datasource = new SoulissDBHelper(getActivity());
		datasource.open();

		Bundle extras = getActivity().getIntent().getExtras();
		if (extras != null && extras.get("TIPICO") != null) {
			collected = (SoulissTypical16AdvancedRGB) extras.get("TIPICO");
		} else if (getArguments() != null) {
			collected = (SoulissTypical16AdvancedRGB) getArguments().get("TIPICO");
		} else {
			Log.e(Constants.TAG, "Error retriving node:");
			return ret;
		}
		assertTrue("TIPICO NULLO", collected instanceof SoulissTypical16AdvancedRGB);
		collected.setPrefs(opzioni);
		collected.setCtx(getActivity());
		if (Constants.versionNumber >= 11) {
			ActionBar actionBar = getActivity().getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle(collected.getNiceName());
		}

		buttPlus = (Button) ret.findViewById(R.id.buttonPlus);
		buttMinus = (Button) ret.findViewById(R.id.buttonMinus);
		togMulticast = (ToggleButton) ret.findViewById(R.id.checkBoxMulticast);

		btOff = (Button) ret.findViewById(R.id.buttonTurnOff);
		btOn = (Button) ret.findViewById(R.id.buttonTurnOn);
		// checkMusic = (CheckBox) ret.findViewById(R.id.checkBoxMusic);
		tableRowChannel = (TableRow) ret.findViewById(R.id.tableRowChannel);
		tableRowEq = (TableRow) ret.findViewById(R.id.tableRowEqualizer);

		btWhite = (Button) ret.findViewById(R.id.white);
		btFlash = (Button) ret.findViewById(R.id.flash);
		btSleep = (Button) ret.findViewById(R.id.sleep);
		modeSpinner = (Spinner) ret.findViewById(R.id.modeSpinner);
		tableRowVis = (TableRow) ret.findViewById(R.id.tableRowMusic);
		mVisualizerView = (VisualizerView) ret.findViewById(R.id.visualizerView);
		mVisualizerView.setOpz(opzioni);
		btEqualizer = (Button) ret.findViewById(R.id.buttonEqualizer);

		seekChannelRed = (SeekBar) ret.findViewById(R.id.channelRed);
		seekChannelGreen = (SeekBar) ret.findViewById(R.id.channelGreen);
		seekChannelBlue = (SeekBar) ret.findViewById(R.id.channelBlue);

		redChanabel = (TextView) ret.findViewById(R.id.channelRedLabel);
		blueChanabel = (TextView) ret.findViewById(R.id.channelBlueLabel);
		greenChanabel = (TextView) ret.findViewById(R.id.channelGreenLabel);
		eqText = (TextView) ret.findViewById(R.id.textEqualizer);

		btOff.setTag(it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_OffCmd);
		btOn.setTag(it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_OnCmd);
		buttPlus.setTag(it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_BrightUp);
		buttMinus.setTag(it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_BrightDown);
		btFlash.setTag(it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_Flash);
		btSleep.setTag(it.angelic.soulissclient.model.typicals.Constants.Souliss_T_related);

		// CHANNEL Listeners
		seekChannelRed.setOnSeekBarChangeListener(new channelInputListener());
		seekChannelGreen.setOnSeekBarChangeListener(new channelInputListener());
		seekChannelBlue.setOnSeekBarChangeListener(new channelInputListener());

		final OnItemSelectedListener lib = new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				 if (pos == 0) {// channels
					tableRowVis.setVisibility(View.GONE);
					mVisualizerView.setVisibility(View.GONE);
					tableRowChannel.setVisibility(View.VISIBLE);
					mVisualizerView.setEnabled(false);
					tableRowEq.setVisibility(View.GONE);
					// TODO questi non vanno
					seekChannelRed.setProgress(Color.red(color));
					seekChannelGreen.setProgress(Color.green(color));
					seekChannelBlue.setProgress(Color.blue(color));
				} else {// music
					if (Constants.versionNumber >= 9) {
						mVisualizerView.setFrag(SingleChannelLedFragment.this);
						mVisualizerView.link(togMulticast.isChecked());
						addBarGraphRenderers();
					} else {
						// TODO scrivere che non esiste
					}
					mVisualizerView.setVisibility(View.VISIBLE);
					tableRowVis.setVisibility(View.VISIBLE);
					mVisualizerView.setEnabled(true);
					mVisualizerView.link(togMulticast.isChecked());

					tableRowEq.setVisibility(View.VISIBLE);
					tableRowChannel.setVisibility(View.GONE);
				}
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		};
		// avoid auto call upon Creation with runnable
		modeSpinner.post(new Runnable() {
			public void run() {
				modeSpinner.setOnItemSelectedListener(lib);
			}
		});

		// Listener generico
		OnClickListener plus = new OnClickListener() {
			public void onClick(View v) {
				Short cmd = (Short) v.getTag();
				assertTrue(cmd != null);

				issueIrCommand(cmd, Color.red(color), Color.green(color), Color.blue(color), togMulticast.isChecked());
				return;
			}

		};

		// Listener generico
		OnClickListener plusEq = new OnClickListener() {
			public void onClick(View v) {
				AlertDialogHelper.equalizerDialog(getActivity(), eqText).show();

				return;
			}

		};

		// start thread x decremento
		OnTouchListener incListener = new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				Short cmd = (Short) v.getTag();
				assertTrue(cmd != null);
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					startIncrementing(cmd);
					break;
				case MotionEvent.ACTION_UP:
					stopIncrementing();
					break;
				}

				return true;
			}

		};
		OnTouchListener decListener = new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				Short cmd = (Short) v.getTag();
				assertTrue(cmd != null);
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					startDecrementing(cmd);
					break;
				case MotionEvent.ACTION_UP:
					stopDecrementing();
					break;
				}

				return true;
			}

		};
		buttPlus.setOnTouchListener(incListener);
		buttMinus.setOnTouchListener(decListener);
		btOff.setOnClickListener(plus);
		btOn.setOnClickListener(plus);
		btEqualizer.setOnClickListener(plusEq);
		btFlash.setOnClickListener(plus);
		btSleep.setOnClickListener(plus);
		
		String strDisease2Format = getResources().getString(R.string.Souliss_TRGB_eq);
		String strDisease2Msg = String.format(strDisease2Format,
				Constants.twoDecimalFormat.format(opzioni.getEqLow()),
				Constants.twoDecimalFormat.format(opzioni.getEqMed()),
				Constants.twoDecimalFormat.format(opzioni.getEqHigh()));
		eqText.setText(strDisease2Msg);
		
		// bianco manuale
		btWhite.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				issueIrCommand(it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_Set, 254, 254, 254,
						togMulticast.isChecked());
				return;
			}
		});

		dialogColorChangedListener = new OnColorChangedListener() {
			/**
			 * {@inheritDoc}
			 */
			public void colorChanged(int c) {
				// Log.i(Constants.TAG, "color changed:" + c);
				color = c;

				// e
				collected.issueRGBCommand(it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_Set,
						Color.red(color), Color.green(color), Color.blue(color), togMulticast.isChecked());
			}
		};

		return ret;
	}

	// Methods for adding renderers to visualizer
	private void addBarGraphRenderers() {
		Paint paint = new Paint();
		paint.setStrokeWidth(50f);
		paint.setAntiAlias(false);
		paint.setColor(Color.argb(255, 156, 138, 252));
		BarGraphRenderer barGraphRendererBottom = new BarGraphRenderer(32, paint, false);
		mVisualizerView.addRenderer(barGraphRendererBottom);

		// TOP
		Paint paint2 = new Paint();
		paint2.setStrokeWidth(12f);
		paint2.setAntiAlias(false);
		paint2.setColor(Color.argb(255, 181, 11, 233));
		BarGraphRenderer barGraphRendererTop = new BarGraphRenderer(4, paint2, true);
		mVisualizerView.addRenderer(barGraphRendererTop);
	}

	public static SingleChannelLedFragment newInstance(int index, SoulissTypical content) {
		SingleChannelLedFragment f = new SingleChannelLedFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putInt("index", index);
		// Ci metto il nodo dentro
		if (content != null) {
			args.putSerializable("TIPICO", (SoulissTypical16AdvancedRGB) content);
		}
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// inflater.inflate(R.menu.queue_options, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				NodeDetailFragment details = NodeDetailFragment.newInstance(collected.getTypicalDTO().getNodeId(),
						collected.getParentNode());
				// Execute a transaction, replacing any existing fragment
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				if (opzioni.isAnimationsEnabled())
					ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
				ft.replace(R.id.details, details);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
				ft.commit();
			} else {
				getActivity().finish();
				if (opzioni.isAnimationsEnabled())
					getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		datasource.open();
		IntentFilter filtere = new IntentFilter();
		filtere.addAction(it.angelic.soulissclient.net.Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
		getActivity().registerReceiver(datareceiver, filtere);
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(datareceiver);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// datasource.close();
		if (mVisualizerView != null)
			mVisualizerView.release();
	}

	// Aggiorna il feedback
	private BroadcastReceiver datareceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// SoulissNode coll = datasource.getSoulissNode();
			collected = (SoulissTypical16AdvancedRGB) datasource.getSoulissTypical(collected.getTypicalDTO()
					.getNodeId(), collected.getTypicalDTO().getSlot());
			// Bundle extras = intent.getExtras();
			// Bundle vers = (Bundle) extras.get("NODES");
			//color = collected.getColor();
			Log.d(Constants.TAG, "Detected data arrival, color change to: R" + Color.red(collected.getColor()) + " G"
					+ Color.green(collected.getColor()) + " B" + Color.blue(collected.getColor()));

		}
	};

	/**
	 * Inner class representing the color Channels.
	 */
	private class channelInputListener implements SeekBar.OnSeekBarChangeListener {

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

			color = Color.argb(255, seekChannelRed.getProgress(), seekChannelGreen.getProgress(),
					seekChannelBlue.getProgress());
			issueIrCommand(it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_Set, Color.red(color),
					Color.green(color), Color.blue(color), togMulticast.isChecked());
			redChanabel.setText(getString(R.string.red) + " - " + Color.red(color));
			greenChanabel.setText(getString(R.string.green) + " - " + Color.green(color));
			blueChanabel.setText(getString(R.string.blue) + " - " + Color.blue(color));
		}

		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		// solo per sicurezza
		public void onStopTrackingTouch(SeekBar seekBar) {
			issueIrCommand(it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_Set, Color.red(color),
					Color.green(color), Color.blue(color), togMulticast.isChecked());
		}

	}

	
	/**************************************************************************
	 * Souliss RGB light command Souliss OUTPUT Data is:
	 * 
	 * 
	 * INPUT data 'read' from GUI
	 **************************************************************************/
	public void issueIrCommand(final short val, final int r, final int g, final int b, final boolean multicast) {
		collected.issueRGBCommand(val, r, g, b, multicast);
	}

}