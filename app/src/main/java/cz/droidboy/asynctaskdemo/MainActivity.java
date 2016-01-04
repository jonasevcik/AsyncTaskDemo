package cz.droidboy.asynctaskdemo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ProgressBar mProgressBar;
    private Button mStartButton;
    private Button mCancelButton;
    private CountingTask mCountingTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mStartButton = (Button) findViewById(R.id.start);
        mCancelButton = (Button) findViewById(R.id.cancel);
    }

    public void startTask(View view) {
        if (mCountingTask == null) {
            mCountingTask = new CountingTask(MainActivity.this);
            mCountingTask.execute();

            mStartButton.setEnabled(false);
            mCancelButton.setEnabled(true);
        }
    }

    public void cancelTask(View view) {
        mCountingTask.cancel(true);
        onTaskFinished();

        Toast.makeText(MainActivity.this, R.string.task_cancelled, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCountingTask != null) {
            mCountingTask.cancel(true);
        }
    }

    public void onTaskFinished() {
        mCountingTask = null;

        mStartButton.setEnabled(true);
        mCancelButton.setEnabled(false);
    }

    private static class CountingTask extends AsyncTask<Void, Integer, Integer> {

        private final WeakReference<MainActivity> mActivityWeakReference;

        public CountingTask(MainActivity mainActivity) {
            mActivityWeakReference = new WeakReference<>(mainActivity);
        }

        private void setProgress(int progress) {
            MainActivity activity = mActivityWeakReference.get();
            if (activity == null) {
                return;
            }
            activity.mProgressBar.setProgress(progress);
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute - thread: " + Thread.currentThread().getName());
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Log.d(TAG, "doInBackground - thread: " + Thread.currentThread().getName());
            for (int i = 0; i <= 100; i++) {
                Log.d(TAG, "iteration: " + i);
                if (isCancelled()) { //try commenting out this if block
                    Log.d(TAG, "cancelling calculation");
                    return null;
                }
                for (long l = 0; l < 10_000_000; l++) {
                } //simulate long running operation
                publishProgress(i);
            }
            return 100;
        }

        @Override
        protected void onProgressUpdate(Integer... values) { // main thread; progress is processed in batches
            Log.d(TAG, "onProgressUpdate - thread: " + Thread.currentThread().getName());
            setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Integer result) {
            Log.d(TAG, "onPostExecute - thread: " + Thread.currentThread().getName());
            setProgress(result);

            MainActivity activity = mActivityWeakReference.get();
            if (activity == null) {
                return;
            }
            activity.onTaskFinished();
        }

        @Override
        protected void onCancelled() {
            Log.d(TAG, "onCancelled - thread: " + Thread.currentThread().getName());
        }
    }

}
