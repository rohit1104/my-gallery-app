package com.rohit.app.test2;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;

public class GalleryViewActivity extends ListActivity {
	private ProgressDialog progressDialog;
	private IdAndLabelEncapsulated[] array;
	private DBHelper dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(true);
		progressDialog.setMessage(getResources().getString(
				R.string.toast_loadingImages));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.show();

		dbHelper = new DBHelper(this);
		new LoadArrayAsyncTask().execute();
	}

	private class LoadArrayAsyncTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			array = dbHelper.getIdAndLabels();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progressDialog.cancel();
			setListAdapter(new ImagesAdapter(GalleryViewActivity.this, array));
		}

	}
}
