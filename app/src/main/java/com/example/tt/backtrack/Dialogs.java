package com.example.tt.backtrack;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/*Contains all of my dialog fragments, used in both MainActivity and ItemList*/
public class Dialogs extends  DialogFragment{

    public Dialogs(){
    }

    public static class NameDialog extends android.support.v4.app.DialogFragment {

        public NameDialog(){
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //use builder class for convenient dialog construction
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View promptView = layoutInflater.inflate(R.layout.input_dialog, null);

            final EditText editText = (EditText) promptView.findViewById(R.id.edit_text);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(promptView);

            builder.setCancelable(false)
                    .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mListener.onInputDialogPositiveClick(editText.getText().toString());
                            //TODO need to pass item back to host for CameraAsk Function
                        }
                    })
                    .setNegativeButton(R.string.Cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });

            return builder.create();
        }

        /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
        public interface NoticeDialogListener {
            public void onInputDialogPositiveClick(String input);
        }

        // Use this instance of the interface to deliver action events
        NoticeDialogListener mListener;

        // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            // Verify that the host activity implements the callback interface
            try {
                // Instantiate the NoticeDialogListener so we can send events to the host
                mListener = (NoticeDialogListener) context;
            } catch (ClassCastException e) {
                // The activity doesn't implement the interface, throw exception
                throw new ClassCastException(context.toString()
                        + " must implement NoticeDialogListener");
            }
        }
    }

    public static class CameraDialog extends android.support.v4.app.DialogFragment {

        public CameraDialog(){
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //use builder class for convenient dialog construction
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View promptView = layoutInflater.inflate(R.layout.camera_dialog, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(promptView);

            builder.setCancelable(false)
                    .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mListener.onCameraDialogPositiveClick(true);
                        }
                    })
                    .setNegativeButton(R.string.Cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mListener.onCameraDialogNegativeClick(false);
                                    dialogInterface.cancel();
                                }
                            });

            return builder.create();
        }

        /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
        public interface NoticeDialogListener {
            public void onCameraDialogPositiveClick(boolean b);
            public void onCameraDialogNegativeClick(boolean b);
        }

        // Use this instance of the interface to deliver action events
        NoticeDialogListener mListener;

        // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            // Verify that the host activity implements the callback interface
            try {
                // Instantiate the NoticeDialogListener so we can send events to the host
                mListener = (NoticeDialogListener) context;
            } catch (ClassCastException e) {
                // The activity doesn't implement the interface, throw exception
                throw new ClassCastException(context.toString()
                        + " must implement NoticeDialogListener");
            }
        }
    }

    public static class DeleteDialog extends android.support.v4.app.DialogFragment {

        public DeleteDialog(){
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //use builder class for convenient dialog construction
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View promptView = layoutInflater.inflate(R.layout.delete_dialog, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(promptView);

            builder.setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mListener.onDeleteDialogPositiveClick();
                        }
                    })
                    .setNegativeButton(R.string.Cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });

            return builder.create();
        }

        /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
         interface NoticeDialogListener {
            public void onDeleteDialogPositiveClick();
        }

        // Use this instance of the interface to deliver action events
        NoticeDialogListener mListener;

        // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            // Verify that the host activity implements the callback interface
            try {
                // Instantiate the NoticeDialogListener so we can send events to the host
                mListener = (NoticeDialogListener) context;
            } catch (ClassCastException e) {
                // The activity doesn't implement the interface, throw exception
                throw new ClassCastException(context.toString()
                        + " must implement NoticeDialogListener");
            }
        }
    }

    public static class NavigateDialog extends android.support.v4.app.DialogFragment {

        public NavigateDialog(){
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //use builder class for convenient dialog construction
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View promptView = layoutInflater.inflate(R.layout.navigate_dialog, null);

            String sUri = getArguments().getString("uri");

            if(sUri != null){
                ImageView image = (ImageView)promptView.findViewById(R.id.navigate_image);
                Uri uri = Uri.parse(sUri);

                Picasso.with(this.getContext())
                        .load(uri)
                        .into(image);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(promptView);

            builder.setCancelable(false)
                    .setPositiveButton("Walk", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mListener.onNavigateDialogWalkClick();
                        }
                    })
                    .setNeutralButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .setNegativeButton("Drive",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mListener.onNavigateDialogDriveClick();
                                }
                            });

            return builder.create();
        }

        /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
        interface NoticeDialogListener {
            public void onNavigateDialogWalkClick();
            public void onNavigateDialogDriveClick();
        }

        // Use this instance of the interface to deliver action events
        NoticeDialogListener mListener;

        // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            // Verify that the host activity implements the callback interface
            try {
                // Instantiate the NoticeDialogListener so we can send events to the host
                mListener = (NoticeDialogListener) context;
            } catch (ClassCastException e) {
                // The activity doesn't implement the interface, throw exception
                throw new ClassCastException(context.toString()
                        + " must implement NoticeDialogListener");
            }
        }

        public static NavigateDialog newInstance(String uri){
            NavigateDialog nd = new NavigateDialog();
            Bundle args = new Bundle();
            args.putString("uri", uri);
            nd.setArguments(args);

            return nd;
        }
    }

    public static class GPSDialog extends android.support.v4.app.DialogFragment {

        public GPSDialog(){
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //use builder class for convenient dialog construction
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View promptView = layoutInflater.inflate(R.layout.gps_dialog, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(promptView);
            builder.setPositiveButton(getActivity().getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int paramInt) {
                    mListener.onGPSDialogPositiveClick();
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton(getActivity().getString(R.string.Cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int paramInt) {
                    dialogInterface.dismiss();
                }
            });

            return builder.create();
        }

        /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
        public interface NoticeDialogListener {
            void onGPSDialogPositiveClick();
        }

        // Use this instance of the interface to deliver action events
        NoticeDialogListener mListener;

        // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            // Verify that the host activity implements the callback interface
            try {
                // Instantiate the NoticeDialogListener so we can send events to the host
                mListener = (NoticeDialogListener) context;
            } catch (ClassCastException e) {
                // The activity doesn't implement the interface, throw exception
                throw new ClassCastException(context.toString()
                        + " must implement NoticeDialogListener");
            }
        }
    }

    public static class PermDialog extends android.support.v4.app.DialogFragment {

        public PermDialog(){
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //use builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Permission Needed")
                    .setMessage("Must have access to location to save it")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mListener.onPermDialogPositiveClick();
                        }
                    });
            return builder.create();
        }

        /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
        public interface NoticeDialogListener {
            void onPermDialogPositiveClick();
        }

        // Use this instance of the interface to deliver action events
        NoticeDialogListener mListener;

        // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            // Verify that the host activity implements the callback interface
            try {
                // Instantiate the NoticeDialogListener so we can send events to the host
                mListener = (NoticeDialogListener) context;
            } catch (ClassCastException e) {
                // The activity doesn't implement the interface, throw exception
                throw new ClassCastException(context.toString()
                        + " must implement NoticeDialogListener");
            }
        }
    }

}
