package kl.law.inspector.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

import kl.law.inspector.R;
import kl.law.inspector.tools.ApiKit;
import kl.law.inspector.tools.UploadFileTask;

import static android.app.Activity.RESULT_OK;

public class Fragment1 extends Fragment {
    private static final int REQUEST_ORIGINAL = 0x01;
    private ImageView previewImage;
    private File previewImageFile;

    public Fragment1() {
        // Required empty public constructor
    }

    public static Fragment1 newInstance() {
        Fragment1 fragment = new Fragment1();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }

        setup();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment1, container, false);
        previewImage = (ImageView) view.findViewById(R.id.preview_image);

        ((Button) view.findViewById(R.id.picture_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File imagePath = new File(getContext().getFilesDir(), "images");
                if(!imagePath.exists()){
                    imagePath.mkdirs();
                }
                File imageFile = new File(imagePath, "f01.avi");
                //File imageFile = new File(imagePath, "20130405_IMG_0820.JPG");
                Log.d("TEST", imageFile.exists()+"=====");

                UploadFileTask task = new UploadFileTask(ApiKit.URL_UPLOAD_IMAGE, imageFile);
                task.execute();


//                MultipartRequest request = new MultipartRequest(ApiKit.URL_UPLOAD_IMAGE, "file", imageFile, new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        Log.d("TEST", "SUCCESS");
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.d("TEST", "ERROR");
//                    }
//                }, null, new MultipartRequest.ProgressListener() {
//                    @Override
//                    public void update(long read, long size, double percent) {
//                        Log.d("TEST", read+" ===== " + size + " ===== " + percent);
//                    }
//                });
//
//                RequestQueue queue = ApiKit.getVolleyRequest(getContext());
//                queue.add(request);
//                queue.start();








//                previewImageFile = CameraKit.createPreviewImageFile(getContext());
//                Uri uri = CameraKit.createPreviewImageUri(getContext(), previewImageFile);
//
//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
//                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//                startActivityForResult(intent, REQUEST_CAPTURE_PICTURE);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ORIGINAL) {
                Bitmap bitmap = BitmapFactory.decodeFile(previewImageFile.getAbsolutePath());
                previewImage.setImageBitmap(bitmap);

//                MultipartRequest request = new MultipartRequest(ApiKit.URL_UPLOAD_IMAGE, "file", previewImageFile, new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        Log.d("TEST", "SUCCESS");
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.d("TEST", "ERROR");
//                    }
//                }, null);
//
//                RequestQueue queue = ApiKit.getVolleyRequest(getContext());
//                queue.add(request);
//                queue.start();
            }
        }
    }

    public void setup() {
        //inflater.inflate(R.menu.navigation, menu);

//        ActionBar bar = ((AppCompatActivity)getActivity()).getSupportActionBar();
//        bar.setTitle("测试");
//        bar.setHomeButtonEnabled(true);
//        bar.setDisplayHomeAsUpEnabled(true);

        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.navigation, menu);
    }
}
