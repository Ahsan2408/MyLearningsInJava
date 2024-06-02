package com.example.mylearingsinjava;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mylearingsinjava.databinding.ActivitySecondBinding;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;

public class SecondActivity extends AppCompatActivity {
    private static final String TAG = "SecondActivity";

    // declare 'ActivitySecondBinding' & 'Uri'  instance variables
    private ActivitySecondBinding binding;
    private static Uri pdfUri;

    // declare a string variable to hold the path of the signature
    private String signaturePath;

    // declare 'PointF' instance variable
    private PointF signatureLocation;

    // initialize 'pdfZoom' to 1.0
    private float pdfZoom = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inflate the layout using view binding
        binding = ActivitySecondBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initialize 'PDFBox' resource loader object
        PDFBoxResourceLoader.init(getApplicationContext());

        // get signature path & PDF URI from the intent
        signaturePath = getIntent().getStringExtra("SIGNATURE_PATH");
        pdfUri = Uri.parse(getIntent().getStringExtra("uri"));

        // set up the 'Sign' button & add signature to the PDF
        setupSignButton();
        addSignatureToPdf();

    }// end 'onCreate' method

    /** show a PDF document from the provided 'URI'
     * @param uri - the URI of the PDF document to be shown */
    private void displayPdf(Uri uri) {

        try {

            // open an input stream from the URI
            InputStream inputStream = getContentResolver().openInputStream(uri);

            // load the PDF document into the 'PDFView' component
            binding.pdfView.fromStream(inputStream)
                    .defaultPage(0) // set the default page to show
                    .onPageChange((page, pageCount) -> updateSignButtonPosition()) // update 'sign' button position on page change
                    .onLoad(nbPages -> updateSignButtonPosition()) // update 'sign' button position on document load
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


    /** add a signature image to the PDF document */
    private void addSignatureToPdf() {

        try {

            // open an input stream from the PDF URI
            InputStream inputStream = getContentResolver().openInputStream(pdfUri);

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
            Bitmap bitmap = BitmapFactory.decodeFile(signaturePath);

            // initialize a 'PDImageXObject' from the 'Bitmap'
            PDImageXObject pdImage = LosslessFactory.createFromImage(document, bitmap);

            // set coordinates for placing signature image on the page
            float x = 150;
            float y = 50;

            // draw the signature image on the page
            contentStream.drawImage(pdImage, x, y, (float) pdImage.getWidth() / 5, (float) pdImage.getHeight() / 5);
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


    /** set up 'Sign' button by defining click listener to show 'sign options dialog' */
    private void setupSignButton() {

        // define the 'Sign' button & set the click listener to show sign options dialog
        binding.btnSign.setOnClickListener(v -> showSignOptionsDialog());

    }// end 'setupSignButton' method


    /** show a dialog to present sign options to the user */
    private void showSignOptionsDialog() {

        // initialize an 'AlertDialog.Builder' instance
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // set the title of the dialog
        builder.setTitle("Sign Document");

        // set the items and actions for the dialog options
        builder.setItems(new String[]{"Draw Signature", "Place Signature from Gallery"}, (dialog, which) -> {

            if (which == 0) {

                // if "Draw Signature" is selected, call the method to draw the signature
                drawSignature();

            } else {

                // if "Place Signature from Gallery" is selected, call the method to pick the signature from the gallery
                pickSignatureFromGallery();

            }// end ELSE

        });// end 'setItems' method

        // set a negative button for canceling the dialog
        builder.setNegativeButton("Cancel", null);

        // create a 'Save' button in the dialog
        builder.setPositiveButton("Save", (dialog, which) -> {

            // save the updated PDF to the 'Downloads' folder
            saveUpdatedPdf();

        });

        // show the dialog
        builder.show();

        // log indicating successful display of sign options dialog
        Log.d(TAG, "Sign options dialog displayed successfully.");

    }// end 'showSignOptionsDialog' method


    /** show a dialog to allow the user to draw a signature */
    private void drawSignature() {

        // initialize an 'AlertDialog.Builder' instance
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // initialize a 'SignatureView' instance
        SignatureView signatureView = new SignatureView(this, null);

        // set the view of the dialog to the 'SignatureView' object
        builder.setView(signatureView);

        // set the title of the dialog
        builder.setTitle("Draw Signature");

        // set the positive button action to save the drawn signature
        builder.setPositiveButton("Save", (dialog, which) -> {

            // get the signature bitmap from the 'SignatureView' object
            Bitmap signatureBitmap = signatureView.getSignatureBitmap();

            // set the signature bitmap
            saveSignatureBitmap(signatureBitmap);

        });// end 'setPositiveButton' method

        // set the negative button action to clear the drawn signature
        builder.setNegativeButton("Clear", (dialog, which) -> signatureView.clear());

        // set the neutral button action to dismiss the dialog
        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        // initialize & show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // log indicating successful display of the draw signature dialog
        Log.d(TAG, "Draw signature dialog displayed successfully.");

    }// end 'drawSignature' method


    /** save the signature bitmap as a temporary file
     * @param bitmap - the bitmap of the signature to be saved */
    private void saveSignatureBitmap(Bitmap bitmap) {

        try {

            // initialize a temporary file to save the signature bitmap
            File tempFile = File.createTempFile("signature", ".png", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            // compress & write the bitmap to the temporary file
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();

            // update the signaturePath with the path of the temporary file
            signaturePath = tempFile.getAbsolutePath();

            // log the successful saving of the signature bitmap
            Log.d(TAG, "Signature bitmap saved to: " + signaturePath);

            // update the signature on the PDF
            updateSignatureOnPdf();

        } catch (Exception e) {

            // log an error message if there is an exception while saving the signature bitmap
            Log.e(TAG, "Error saving signature bitmap", e);

            // show a toast message indicating the error in saving the signature
            Toast.makeText(this, "Error saving signature", Toast.LENGTH_LONG).show();

        }// end CATCH

    }// end 'drawSignature' method


    /** update the signature on the PDF */
    private void updateSignatureOnPdf() {

        try {

            // open an input stream to read the PDF file
            InputStream inputStream = getContentResolver().openInputStream(pdfUri);

            // load the PDF document
            PDDocument document = PDDocument.load(inputStream);

            // get the first page of the PDF
            PDPage page = document.getPage(0);

            // initialize a content stream to append content to the PDF page
            PDPageContentStream contentStream = new PDPageContentStream(document,
                    page,
                    PDPageContentStream.AppendMode.APPEND,
                    true);

            // decode the signature bitmap file to a Bitmap object
            Bitmap bitmap = BitmapFactory.decodeFile(signaturePath);

            // initialize a 'PDImageXObject' from the decoded bitmap
            PDImageXObject pdImage = LosslessFactory.createFromImage(document, bitmap);

            // set position & size of signature image on the PDF page
            float x = 150;
            float y = 50;

            // draw the signature image on the PDF page
            contentStream.drawImage(pdImage, x, y,
                    (float) pdImage.getWidth() / 5,
                    (float) pdImage.getHeight() / 5);

            // close the content stream
            contentStream.close();

            // save the updated PDF file to the 'Downloads' folder
            File updatedPdfFile = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "updated.pdf");
            document.save(updatedPdfFile);
            document.close();
            if (inputStream != null) {

                inputStream.close();

            }// end IF

            // log the path of the updated PDF file
            Log.d(TAG, "Updated PDF path: " + updatedPdfFile.getAbsolutePath());

            // update the signature location
            signatureLocation = new PointF(x, y);

            // show the updated PDF
            pdfUri = Uri.fromFile(updatedPdfFile);
            displayPdf(pdfUri);

            // show a toast message indicating the successful update of the signature on the PDF
            Toast.makeText(this, "Signature updated on PDF", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {

            // log an error message if there is an exception while updating the signature on the PDF
            Log.e(TAG, "Error updating signature on PDF", e);

            // show a toast message indicating the error in updating the signature
            Toast.makeText(this, "Error updating signature", Toast.LENGTH_LONG).show();

        }// end CATCH

    }// end 'updateSignatureOnPdf' method


    /** save the updated PDF to the 'Download' folder */
    private void saveUpdatedPdf() {

        try {

            // generate a unique file name based on the current timestamp
            String fileName = "updated_" + System.currentTimeMillis() + ".pdf";

            // reference the 'Downloads' directory for saving PDF files
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            // initialize the updated PDF file
            File updatedPdfFile = new File(downloadDir, fileName);

            // initialize a 'FileOutputStream' object for the updated file
            FileOutputStream outputStream = new FileOutputStream(updatedPdfFile);

            // load updated PDF document from the current Uri
            PDDocument document = PDDocument.load(new File(Objects.requireNonNull(pdfUri.getPath())));

            // save updated PDF document to the 'FileOutputStream' object
            document.save(outputStream);
            document.close();

            // close the 'FileOutputStream' object
            outputStream.close();

            // log the path of the saved PDF file
            Log.d(TAG, "Saved PDF path: " + updatedPdfFile.getAbsolutePath());

            // show a toast message indicating successful saving
            Toast.makeText(this, "PDF saved to Downloads folder", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {

            // log & show a toast message if an error occurs
            Log.e(TAG, "Error saving PDF", e);
            Toast.makeText(this, "Error saving PDF", Toast.LENGTH_LONG).show();

        }// end CATCH

    }// end 'saveUpdatedPdf' method


    /** pick a signature image from the gallery */
    private void pickSignatureFromGallery() {

        try {

            // initialize an intent to pick an image from the gallery
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");

            // start the activity to select an image from the gallery
            startActivityForResult(intent, 2);

        } catch (Exception e) {

            // log an error message if there is an exception while picking a signature from the gallery
            Log.e(TAG, "Error picking signature from gallery", e);

            // show a toast message indicating the error in picking the signature
            Toast.makeText(this, "Error picking signature", Toast.LENGTH_LONG).show();

        }// end CATCH

    }// end 'pickSignatureFromGallery' method


    /** update the position of the 'sign' button on the screen */
    private void updateSignButtonPosition() {

        try {

            // check if the signature location is not null
            if (signatureLocation == null) {

                return; // if null, return without updating the sign button position

            }// end IF

            // convert PDF coordinates to screen coordinates
            float[] screenPos = pdfToScreenCoordinates(signatureLocation.x, signatureLocation.y);

            // check if the 'screenPos' is not null
            if (screenPos != null) {

                // set the X and Y position of the sign button
                binding.btnSign.setX(screenPos[0]);
                binding.btnSign.setY(screenPos[1]);

                // Log the updated position of the sign button
                Log.d(TAG, "Sign button position updated: X = " + screenPos[0] + ", Y = " + screenPos[1]);

                // make the sign button visible
                binding.btnSign.setVisibility(View.VISIBLE);

            }// end IF

        } catch (Exception e) {

            // log an error message if there is an exception while updating the sign button position
            Log.e(TAG, "Error updating sign button position", e);

        }// end CATCH


    }// end 'updateSignButtonPosition' method


    /** convert PDF coordinates to screen coordinates
     * @param pdfX The X coordinate in PDF units.
     * @param pdfY The Y coordinate in PDF units.
     * @return An array containing the converted X and Y screen coordinates */
    private float[] pdfToScreenCoordinates(float pdfX, float pdfY) {

        try {

            // get the offset & zoom values from the PDF view
            float offsetX = binding.pdfView.getPositionOffset();
            float offsetY = binding.pdfView.getPositionOffset();
            float zoom = binding.pdfView.getZoom();

            // calculate the screen coordinates
            float screenX = pdfX * zoom + offsetX;
            float screenY = pdfY * zoom + offsetY;

            // log the screen coordinates
            Log.d(TAG, "Screen coordinates calculated: X = " + screenX + ", Y = " + screenY);

            // return an array containing the screen coordinates
            return new float[]{screenX, screenY};

        } catch (Exception e) {

            // log an error message if there is an exception while converting PDF coordinates to screen coordinates
            Log.e(TAG, "Error converting PDF coordinates to screen coordinates", e);

            // return null in case of an error
            return null;

        }// end CATCH


    }// end 'pdfToScreenCoordinates' method


    /** get the actual file path from the given URI
     * @param uri - the URI of the file.
     * @return - the actual file path */
    private String getPathFromUri(Uri uri) {

        try {

            // initialize the file path variable to null
            String filePath = null;

            // define the projection for the query
            String[] projection = {MediaStore.Images.Media.DATA};

            // perform a query to get the cursor
            Cursor cursor = getContentResolver().query(uri,
                    projection,
                    null,
                    null,
                    null);

            // check if the cursor is not null
            if (cursor != null) {

                // move the cursor to the first row
                cursor.moveToFirst();

                // get the index of the file path column
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                // get the file path from the cursor
                filePath = cursor.getString(columnIndex);

                // close the cursor
                cursor.close();

            }// end IF

            // log the file path
            Log.d(TAG, "File path retrieved: " + filePath);

            // return the file path
            return filePath;

        } catch (Exception e) {

            // log an error message if there is an exception while getting the file path from URI
            Log.e(TAG, "Error getting file path from URI", e);

            // return null in case of an error
            return null;

        }// end CATCH


    }// end 'getPathFromUri' method


    /** handle the result of an activity.
     * @param requestCode - The request code.
     * @param resultCode  - The result code.
     * @param data       -  The intent data */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        try {

            // call superclass method to handle the activity result
            super.onActivityResult(requestCode, resultCode, data);

            // check if the result is OK and the data is not null
            if (resultCode == RESULT_OK && data != null) {

                // check if the request code matches the signature pick request code
                if (requestCode == 2) {

                    // reference the URI of the selected image from the intent data
                    Uri selectedImageUri = data.getData();

                    // check if the selected image URI is not null
                    if (selectedImageUri != null) {

                        // reference the actual file path from the selected image URI
                        signaturePath = getPathFromUri(selectedImageUri);

                        // log the selected signature path
                        Log.d(TAG, "Selected signature path: " + signaturePath);

                        // re-add signature with the new image
                        addSignatureToPdf();

                    }// end 2nd inner IF

                }// end 1st inner IF

            }// end IF

        } catch (Exception e) {

            // log an error message if there is an exception while handling the activity result
            Log.e(TAG, "Error handling activity result", e);

        }// end CATCH

    }// end 'onActivityResult' override method

}// end 'SecondActivity' class