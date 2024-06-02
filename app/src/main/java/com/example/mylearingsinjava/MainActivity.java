package com.example.mylearingsinjava;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mylearingsinjava.databinding.ActivityMainBinding;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private static final int PICK_PDF_FILE = 1;
    private static Uri pdfUri;
    private PointF signatureLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnPickDocument.setOnClickListener(v ->
        {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            startActivityForResult(intent, PICK_PDF_FILE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_PDF_FILE) {
                Uri uri = data.getData();
                addSignatureBoxToPdf(uri);
            }
        }
    }


    /** add a signature image to the PDF document */
    private void addSignatureBoxToPdf(Uri uri) {

        try {

            // open an input stream from the PDF URI
            InputStream inputStream = getContentResolver().openInputStream(uri);

            // load the PDF document
            PDDocument document = PDDocument.load(inputStream);

            // reference the first page of the document
            PDPage page = document.getPage(0);

            // initialize a content stream to add the signature image to the page
            PDPageContentStream contentStream = new PDPageContentStream(document,
                    page,
                    PDPageContentStream.AppendMode.APPEND,
                    true);

            // decode the signature image file into a 'Bitmap'
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img);

            // initialize a 'PDImageXObject' from the 'Bitmap'
            PDImageXObject pdImage = LosslessFactory.createFromImage(document, bitmap);

            // set coordinates for placing signature image on the page
            float x = 300;
            float y = 75;

            // draw the signature image on the page
            contentStream.drawImage(pdImage, x, y, (float) pdImage.getWidth() / 3, (float) pdImage.getHeight() / 3);
            contentStream.close();

            // save updated PDF document
            File file = new File(getExternalFilesDir(null), "updated.pdf");
            document.save(file);
            document.close();
            if (inputStream != null) {

                inputStream.close();

            }// end IF

            // update the signature location
            signatureLocation = new PointF(x, y);

            // show a log indicating the signature has been added
            Log.d(TAG, "Signature added to PDF");

            // show a toast message indicating the signature has been added
            Toast.makeText(this, "Signature added", Toast.LENGTH_SHORT).show();

            // update the PDF URI & show the updated PDF
            pdfUri = Uri.fromFile(file);
            displayPdf(pdfUri);

        } catch (Exception e) {

            // log an error message if there is an exception while adding the signature
            Log.e(TAG, "Error adding signature to PDF", e);

            // show a toast message indicating the error in adding the signature
            Toast.makeText(this, "Error adding signature", Toast.LENGTH_LONG).show();

        }// end CATCH

    }// end 'addSignatureToPdf' method

    /** show a PDF document from the provided 'URI'
     * @param uri - the URI of the PDF document to be shown */
    private void displayPdf(Uri uri) {

        try {

            // open an input stream from the URI
            InputStream inputStream = getContentResolver().openInputStream(uri);
            binding.btnPickDocument.setVisibility(View.GONE);

            // load the PDF document into the 'PDFView' component
            binding.pdfView.fromStream(inputStream)
                    .defaultPage(0) // set the default page to show
                    .scrollHandle(new DefaultScrollHandle(this)) // set up scroll handle for scrolling through pages
                    .spacing(5) // set spacing between pages
                    .load(); // load the PDF document

            // log indicating successful display of the PDF document
            Log.d(TAG, "PDF document displayed successfully.");

        } catch (Exception e) {

            // log and display error message if there is a problem loading the PDF
            Log.e(TAG, "Error loading PDF", e);
            Toast.makeText(this, "Error loading PDF", Toast.LENGTH_LONG).show();

        }// end CATCH

    }// end 'displayPdf' method
}