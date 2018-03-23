package uk.co.tezk.trainspotter.base;

public interface TrainspotterDialogSupport {
    /**
     * Implemented by parent Activity to allow attached Fragments to display progress Dialog
     */
        void showProgressDialog() ;
        void hideProgressDialog() ;

        void showErrorMessage(String message) ;
    }