package com.example.mylearingsinjava;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mylearingsinjava.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private static final int PICK_PDF_FILE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnClear.setOnClickListener(v -> binding.signatureView.clear());

        binding.btnNext.setOnClickListener(v ->
        {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            startActivityForResult(intent, PICK_PDF_FILE);
        });

        binding.btnLoad.setOnClickListener(v -> {
            Bitmap signBitmap = binding.signatureView.getTransparentSignatureBitmap();
            if (signBitmap != null) {
                binding.ivSignature.setImageBitmap(signBitmap);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_PDF_FILE) {
                Uri uri = data.getData();
                Intent intent = new Intent(this, SecondActivity.class);
                if (uri != null) {
                    if (!binding.signatureView.isEmpty()) {
                        intent.putExtra("uri", uri.toString());
                        Bitmap signBitmap = binding.signatureView.getTransparentSignatureBitmap();
                        File outputSignature = saveSignature(signBitmap);
                        intent.putExtra("SIGNATURE_PATH", Objects.requireNonNull(outputSignature).getAbsolutePath());
                        startActivity(intent);
                    }
                }
            }
        }
    }

    private void saveBitmapToPNG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        stream.close();
    }

    private File saveSignature(Bitmap signature) {
        try {
            File output = File.createTempFile("signer", ".png", getApplicationContext().getCacheDir());
            saveBitmapToPNG(signature, output);
            return output;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}