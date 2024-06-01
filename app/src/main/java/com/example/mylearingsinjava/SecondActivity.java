package com.example.mylearingsinjava;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mylearingsinjava.databinding.ActivitySecondBinding;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnTapListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDActionURI;
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;

public class SecondActivity extends AppCompatActivity {
    private static final String TAG = "SecondActivity";
    private ActivitySecondBinding binding;
    private static Uri pdfUri;
    private String signaturePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySecondBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        PDFBoxResourceLoader.init(getApplicationContext());
        signaturePath = getIntent().getStringExtra("SIGNATURE_PATH");
        pdfUri = Uri.parse(getIntent().getStringExtra("uri"));

        addSignatureToPdf();
    }

    private void displayPdf(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);

            binding.pdfView.fromStream(inputStream)
                    .defaultPage(0)
                    .scrollHandle(new DefaultScrollHandle(this))
                    .spacing(5)
                    .onTap(e -> {
                        checkSignatureLocation(e.getX(), e.getY()); // Check location on tap
                        return false;
                    })
                    .load();

        } catch (Exception e) {
            Log.e(TAG, "Error loading PDF", e);
            Toast.makeText(this, "Error loading PDF", Toast.LENGTH_LONG).show();
        }
    }

    private void addSignatureToPdf() {
        try {
            InputStream inputStream = getContentResolver().openInputStream(pdfUri);
            PDDocument document = PDDocument.load(inputStream);
            PDDocumentInformation info = document.getDocumentInformation();

                        // Add signature
            PDPage page = document.getPage(0);
            PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);

            // Load signature image
            Bitmap bitmap = BitmapFactory.decodeFile(signaturePath);
            PDImageXObject pdImage = LosslessFactory.createFromImage(document, bitmap);

            // Draw signature image on PDF
            contentStream.drawImage(pdImage, 150, 50, (float) pdImage.getWidth() / 5, (float) pdImage.getHeight() / 5);
            contentStream.close();


            // Save the document
            File file = new File(getExternalFilesDir(null), "updated.pdf");
            document.save(file);
            document.close();
            inputStream.close();

            Toast.makeText(this, "Signature added", Toast.LENGTH_SHORT).show();
            pdfUri = Uri.fromFile(file);
            displayPdf(pdfUri);

        } catch (Exception e) {
            Log.e(TAG, "Error adding signature to PDF", e);
            Toast.makeText(this, "Error adding signature", Toast.LENGTH_LONG).show();
        }
    }

    private void checkSignatureLocation(float x, float y) {
//        if (signatureLocation != null) {
//            float threshold = 50; // Adjust threshold as needed
//
//            // Check if tap is within threshold of signature location
//            if (Math.abs(x - signatureLocation.x) <= threshold && Math.abs(y - signatureLocation.y) <= threshold) {
//                Log.e(TAG, "Signature clicked");
//            } else {
//                Log.e(TAG, "Outside of signature");
//            }
//        }
    }
}