package com.example.helloandroidh2;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Our Android UI activity which displays a text window when it is run.
 */
public class HelloAndroidH2 extends Activity {

	private final String LOG_TAG = getClass().getSimpleName();
	private ConnectionSource connectionSource;
	private Dao<SimpleData, Integer> simpleDao;

	{
		if (connectionSource == null) {
			try {
				connectionSource =
						new JdbcConnectionSource(
								"jdbc:h2:/data/data/com.example.helloandroidh2/databases/helloAndroidH2");
				simpleDao = DaoManager.createDao(connectionSource, SimpleData.class);
			} catch (SQLException e) {
				throw new RuntimeException("Problems initializing database objects", e);
			}
			try {
				TableUtils.createTable(connectionSource, SimpleData.class);
			} catch (SQLException e) {
				// ignored
			}
		}
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(LOG_TAG, "creating " + getClass());
		TextView tv = new TextView(this);
		doSampleDatabaseStuff("onCreate", tv);
		setContentView(tv);
	}

	/**
	 * Do our sample database stuff.
	 */
	private void doSampleDatabaseStuff(String action, TextView tv) {
		try {
			// query for all of the data objects in the database
			List<SimpleData> list = simpleDao.queryForAll();
			// our string builder for building the content-view
			StringBuilder sb = new StringBuilder();
			sb.append("got ").append(list.size()).append(" entries in ").append(action).append("\n");

			// if we already have items in the database
			int simpleC = 0;
			for (SimpleData simple : list) {
				sb.append("------------------------------------------\n");
				sb.append("[" + simpleC + "] = ").append(simple).append("\n");
				simpleC++;
			}
			sb.append("------------------------------------------\n");
			for (SimpleData simple : list) {
				int ret = simpleDao.delete(simple);
				sb.append("deleted id " + simple.id + " returned ").append(ret).append("\n");
				Log.i(LOG_TAG, "deleting simple(" + simple.id + ") returned " + ret);
				simpleC++;
			}

			int createNum;
			do {
				createNum = new Random().nextInt(3) + 1;
			} while (createNum == list.size());
			for (int i = 0; i < createNum; i++) {
				// create a new simple object
				long millis = System.currentTimeMillis();
				SimpleData simple = new SimpleData(millis);
				// store it in the database
				simpleDao.create(simple);
				Log.i(LOG_TAG, "created simple(" + millis + ")");
				// output it
				sb.append("------------------------------------------\n");
				sb.append("created new entry #").append(i + 1).append(":\n");
				sb.append(simple).append("\n");
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					// ignore
				}
			}

			tv.setText(sb.toString());
		} catch (SQLException e) {
			Log.e(LOG_TAG, "Database exception", e);
			tv.setText("Database exeption: " + e.getMessage());
			return;
		}
	}
}
