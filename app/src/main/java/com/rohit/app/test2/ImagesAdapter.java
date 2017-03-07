package com.rohit.app.test2;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImagesAdapter extends ArrayAdapter<IdAndLabelEncapsulated> {
	private IdAndLabelEncapsulated[] array;
	private Context context;
	private DBHelper dbHelper;

	public ImagesAdapter(Context _context, IdAndLabelEncapsulated[] objects) {
		super(_context, -1, objects);
		array = objects;
		context = _context;
		dbHelper = new DBHelper(_context);
	}

	private class ViewHolder {
		TextView label;
		ImageView image;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = ((Activity) context).getLayoutInflater().inflate(
					R.layout.row_layout, parent, false);

			viewHolder = new ViewHolder();
			viewHolder.label = (TextView) convertView
					.findViewById(R.id.row_label);
			viewHolder.image = (ImageView) convertView
					.findViewById(R.id.row_image);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		IdAndLabelEncapsulated object = array[position];
		if (object != null) {
			viewHolder.label.setText(object.getLabel().toString());
			ImageviewAndIdEncapsulated obj = new ImageviewAndIdEncapsulated();
			obj.id = object.getId();
			obj.imageView = viewHolder.image;
			viewHolder.image.setImageBitmap(null);
			new LoadImageAsyncTask()
					.execute(new ImageviewAndIdEncapsulated[] { obj });
		}
		return convertView;
	}

	private class LoadImageAsyncTask extends
			AsyncTask<ImageviewAndIdEncapsulated, Void, Bitmap> {
		private ImageView imageView;

		@Override
		protected Bitmap doInBackground(ImageviewAndIdEncapsulated... params) {
			imageView = params[0].imageView;
			return dbHelper
					.getImage(
							params[0].id,
							Double.valueOf(
									context.getResources().getDisplayMetrics().density * 100)
									.intValue());
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			imageView.setImageBitmap(result);
		}
	}

	private class ImageviewAndIdEncapsulated {
		ImageView imageView;
		Integer id;
	}
}
