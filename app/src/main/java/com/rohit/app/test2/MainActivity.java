package com.rohit.app.test2;

import java.io.FileNotFoundException;
import java.io.InputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private final static int SELECT_PHOTO_FROM_SD_CARD = 1, CLICK_PHOTO = 2;
	private ImageView imageView;
	private Button saveButton, cancelButton;
	private EditText label;
	private Bitmap bitmap;
	private DBHelper dbHelper;
	private String labeltext;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		dbHelper = new DBHelper(this);

		imageView = (ImageView) findViewById(R.id.imageview_imagePreview);
		saveButton = (Button) findViewById(R.id.button_save);
		cancelButton = (Button) findViewById(R.id.button_cancel);
		label = (EditText) findViewById(R.id.edittext_label);

		hideImageUI();

		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				labeltext = label.getText().toString();
				if (labeltext.trim().equals("")) {
					Toast.makeText(
							MainActivity.this,
							getResources().getString(R.string.toast_emptyLabel),
							Toast.LENGTH_LONG).show();
				} else {
					new SaveImageAsyncTask().execute();
					progressDialog = new ProgressDialog(MainActivity.this);
					progressDialog.setCancelable(true);
					progressDialog.setMessage(getResources().getString(
							R.string.progressDialog_savingImage));
					progressDialog
							.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					progressDialog.show();
				}
			}
		});

		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				hideImageUI();
			}
		});

		Button loadImageFromSdCard = (Button) findViewById(R.id.button_loadImageFromSdCard);
		loadImageFromSdCard.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				if (photoPickerIntent.resolveActivity(getPackageManager()) == null) {
					Toast.makeText(
							MainActivity.this,
							getResources()
									.getString(
											R.string.toast_noResolverForPhotoPickerIntent),
							Toast.LENGTH_LONG).show();
				} else {
					startActivityForResult(photoPickerIntent,
							SELECT_PHOTO_FROM_SD_CARD);
				}
			}
		});

		Button loadImageFromCamera = (Button) findViewById(R.id.button_loadImageFromCamera);
		if (!getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			loadImageFromCamera.setEnabled(false);
		}
		loadImageFromCamera.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent takePictureIntent = new Intent(
						MediaStore.ACTION_IMAGE_CAPTURE);
				if (takePictureIntent.resolveActivity(getPackageManager()) == null) {
					Toast.makeText(
							MainActivity.this,
							getResources()
									.getString(
											R.string.toast_noResolverForTakePictureIntent),
							Toast.LENGTH_LONG).show();
				} else {
					startActivityForResult(takePictureIntent, CLICK_PHOTO);
				}
			}
		});

		Button showMyGalleryButton = (Button) findViewById(R.id.button_showMyGallery);
		showMyGalleryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				progressDialog = new ProgressDialog(MainActivity.this);
				progressDialog.setCancelable(true);
				progressDialog.setMessage(getResources().getString(
						R.string.toast_lookingUpDatabase));
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.show();
				new CheckIfImagesExistAsyncTask().execute();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case SELECT_PHOTO_FROM_SD_CARD:
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				progressDialog = new ProgressDialog(MainActivity.this);
				progressDialog.setCancelable(true);
				progressDialog.setMessage(getResources().getString(
						R.string.toast_loading));
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.show();
				new LoadImageSDCard().execute(new Uri[] { uri });
			} else {
				Toast.makeText(
						this,
						getResources().getString(
								R.string.toast_resultNotOkForIntents),
						Toast.LENGTH_LONG).show();
			}
			break;
		case CLICK_PHOTO:
			if (resultCode == RESULT_OK) {
				Bundle extras = data.getExtras();
				bitmap = (Bitmap) extras.get("data");
				setImageUI(bitmap);
			} else {
				Toast.makeText(
						this,
						getResources().getString(
								R.string.toast_resultNotOkForIntents),
						Toast.LENGTH_LONG).show();
			}
			break;
		}
	}

	private void hideImageUI() {
		imageView.setImageDrawable(getResources().getDrawable(
				R.drawable.no_image_selected));
		cancelButton.setVisibility(View.GONE);
		saveButton.setVisibility(View.GONE);
		label.setVisibility(View.GONE);
	}

	private void setImageUI(Bitmap bitmap) {
		imageView.setImageBitmap(bitmap);
		saveButton.setVisibility(View.VISIBLE);
		cancelButton.setVisibility(View.VISIBLE);
		label.setVisibility(View.VISIBLE);
		label.requestFocus();
		label.setText("");
		label.setHint(getResources().getString(
				R.string.MainActivity_edittext_label_hint));
	}

	private class SaveImageAsyncTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			dbHelper.addImage(labeltext, bitmap);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			hideImageUI();
			Toast.makeText(MainActivity.this,
					getResources().getString(R.string.toast_imageSaved),
					Toast.LENGTH_LONG).show();
			progressDialog.cancel();
		}

	}

	private class CheckIfImagesExistAsyncTask extends
			AsyncTask<Void, Void, Integer> {
		@Override
		protected Integer doInBackground(Void... arg0) {
			return dbHelper.numberOfRows();
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			progressDialog.cancel();
			if (result == 0) {
				Toast.makeText(
						MainActivity.this,
						getResources().getString(
								R.string.toast_noImageInDatabase),
						Toast.LENGTH_LONG).show();
			} else {
				Intent intent = new Intent(MainActivity.this,
						GalleryViewActivity.class);
				startActivity(intent);
			}
		}
	}

	private class LoadImageSDCard extends AsyncTask<Uri, Void, Void> {
		@Override
		protected Void doInBackground(Uri... params) {
			try {
				InputStream imageStream = getContentResolver().openInputStream(
						params[0]);
				bitmap = BitmapFactory.decodeStream(imageStream);

			} catch (FileNotFoundException e) {
				Log.e(getClass().getName(), e.getLocalizedMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progressDialog.cancel();
			setImageUI(bitmap);
		}

	}
}
