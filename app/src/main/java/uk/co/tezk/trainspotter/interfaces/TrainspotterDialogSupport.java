package uk.co.tezk.trainspotter.interfaces;

public interface TrainspotterDialogSupport {
        void startProgressDialog() ;
        void stopProgressDialog() ;

        void showErrorMessage(String message) ;
    }