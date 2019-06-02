package com.stats.restverticle;

import java.awt.GridLayout;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.knowm.xchart.BubbleChart;
import org.knowm.xchart.BubbleChartBuilder;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.demo.charts.ExampleChart;
import org.knowm.xchart.demo.charts.RealtimeExampleChart;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.Styler.ChartTheme;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class GraphVerticle extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(GraphVerticle.class);

	private BubbleChart bubbleChart;

	private List<Double> yData;
	private List<Double> bubbleData;
	public static final String SERIES_NAME = "LinearRegression";
	XYChart mChart = null;
	SwingWrapper<XYChart> mSwingWrapper = null;
	private AtomicBoolean isEnabled = new AtomicBoolean(false);

	private XYTimeChart xyChart = new XYTimeChart();
	final XChartPanel<XYChart> chartPanel = xyChart.buildPanel();
	final XChartPanel<BubbleChart> chartPanel2 = buildPanel(true);

	/**
	 * Invoked when this verticle is deployed. (Life cycle method to start the
	 * verticle)
	 */
	@Override
	public void start() throws Exception {

		vertx.eventBus().consumer("pool-config", message -> {
			String msgBody = (String) message.body(); // Caution - does this cause any issues ??

			if (msgBody.contains("StartPublish")) {
				logger.info("Begin: Collecting data for machine learning");
				isEnabled.set(true);
			}
			if (msgBody.contains("StopPublish")) {
				logger.info("Stopeed collecting data for machine learning");
				isEnabled.set(false);
			}
		});

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				GridLayout myLayout = new GridLayout(1, 2);

				// Create and set up the window.
				JFrame frame = new JFrame("XChart");
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				frame.setLayout(myLayout);

				frame.add(chartPanel);
				frame.add(chartPanel2);

				// Display the window.
				frame.pack();
				frame.setVisible(true);
			}
		});

		vertx.setPeriodic(1600, id -> {

			if (!isEnabled.get()) {
				String message = "Not rendering graph data - graphVerticle";
				logger.info(message);
				vertx.eventBus().publish("events-feed", "Graph not rendering..");
				return;
			}

			try {
				xyChart.updateData();
				updateData();
				chartPanel.revalidate();
				chartPanel.repaint();
				chartPanel2.revalidate();
				chartPanel2.repaint();
				vertx.eventBus().publish("events-feed", "Graph rendered , navigate to jpanel for realtime view..");
			} catch (Exception e) {
				logger.error("Encountered exception while rendering graph " + e);
			}
		});

	}

	private XChartPanel<BubbleChart> buildPanel(boolean b) {
		return new XChartPanel<BubbleChart>(getChart());
	}

	public BubbleChart getChart() {

		yData = getRandomData(5);
		bubbleData = getRandomData(5);

		// Create Chart
		bubbleChart = new BubbleChartBuilder().width(500).height(400).theme(ChartTheme.GGPlot2).xAxisTitle("X")
				.yAxisTitle("Y").title("Real-time Bubble Chart").build();

		bubbleChart.addSeries(SERIES_NAME, null, yData, bubbleData);

		return bubbleChart;
	}

	private List<Double> getRandomData(int numPoints) {

		List<Double> data = new CopyOnWriteArrayList<Double>();
		for (int i = 0; i < numPoints; i++) {
			data.add(Math.random() * 100);
		}
		return data;
	}

	public void updateData() {

		// Get some new data
		List<Double> newData = getRandomData(1);
		yData.addAll(newData);
		// Limit the total number of points
		while (yData.size() > 20) {
			yData.remove(0);
		}

		// Get some new data
		newData = getRandomData(1);
		bubbleData.addAll(newData);
		// Limit the total number of points
		while (bubbleData.size() > 20) {
			bubbleData.remove(0);
		}
		bubbleChart.updateBubbleSeries(SERIES_NAME, null, yData, bubbleData);
	}

	private SwingWrapper<XYChart> getSwingWrapper(final XYChart chart) {
		if (mSwingWrapper == null) {
			mSwingWrapper = (SwingWrapper<XYChart>) new SwingWrapper((Chart) chart);
			mSwingWrapper.displayChart();
		}
		return mSwingWrapper;
	}

	private XYChart getChartInstance(final double[][] initdata) {
		if (mChart == null)
			mChart = QuickChart.getChart("Simple XChart Real-time Demo", "Radians", "Sine", "sine", initdata[0],
					initdata[1]);
		return mChart;
	}

}

/**
 * Real-time XY Chart with Error Bars
 *
 * <p>
 * Demonstrates the following:
 *
 * <ul>
 * <li>real-time chart updates with JFrame
 * <li>fixed window
 * <li>error bars
 */
class XYTimeChart implements ExampleChart<XYChart>, RealtimeExampleChart {

	private XYChart xyChart;

	private List<Integer> xData = new CopyOnWriteArrayList<Integer>();
	private final List<Double> yData = new CopyOnWriteArrayList<Double>();
	private List<Double> errorBars = new CopyOnWriteArrayList<Double>();

	public static final String SERIES_NAME = "regressionSeries";

	public static void main(String[] args) {

		// Setup the panel
		final XYTimeChart realtimeChart03 = new XYTimeChart();
		final XChartPanel<XYChart> chartPanel = realtimeChart03.buildPanel();

		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				// Create and set up the window.
				JFrame frame = new JFrame("XChart");
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				frame.add(chartPanel);

				// Display the window.
				frame.pack();
				frame.setVisible(true);
			}
		});

		// Simulate a data feed
		TimerTask chartUpdaterTask = new TimerTask() {

			@Override
			public void run() {

				realtimeChart03.updateData();
				chartPanel.revalidate();
				chartPanel.repaint();
			}
		};

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(chartUpdaterTask, 0, 500);
	}

	public XChartPanel<XYChart> buildPanel() {

		return new XChartPanel<XYChart>(getChart());
	}

	@Override
	public XYChart getChart() {

		yData.add(0.0);
		for (int i = 0; i < 50; i++) {
			double lastPoint = yData.get(yData.size() - 1);
			yData.add(getRandomWalk(lastPoint));
		}
		// generate X-Data
		xData = new CopyOnWriteArrayList<Integer>();
		for (int i = 1; i < yData.size() + 1; i++) {
			xData.add(i);
		}
		// generate error bars
		errorBars = new CopyOnWriteArrayList<Double>();
		for (int i = 0; i < yData.size(); i++) {
			errorBars.add(20 * Math.random());
		}

		// Create Chart
		xyChart = new XYChartBuilder().width(500).height(400).xAxisTitle("X").yAxisTitle("Y")
				.title("Real-time XY Chart with Error Bars").build();

		xyChart.addSeries(SERIES_NAME, xData, yData, errorBars);

		return xyChart;
	}

	private Double getRandomWalk(double lastPoint) {

		return lastPoint + (Math.random() * 100 - 50);
	}

	public void updateData() {

		// Get some new data
		double lastPoint = yData.get(yData.size() - 1);
		yData.add(getRandomWalk(lastPoint));
		yData.remove(0);

		// update error bars
		errorBars.add(20 * Math.random());
		errorBars.remove(0);

		xyChart.updateXYSeries(SERIES_NAME, xData, yData, errorBars);
	}
}
