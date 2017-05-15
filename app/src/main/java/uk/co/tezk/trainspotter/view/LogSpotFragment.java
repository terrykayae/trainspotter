package uk.co.tezk.trainspotter.view;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.tezk.trainspotter.R;
import uk.co.tezk.trainspotter.model.SightingDetails;
import uk.co.tezk.trainspotter.network.Submitter;
import uk.co.tezk.trainspotter.realm.RealmHandler;

import static uk.co.tezk.trainspotter.model.Constant.FRAG_TAG_DATE_PICKER;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LogSpotFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LogSpotFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LogSpotFragment extends Fragment implements OnMapReadyCallback {

    private OnFragmentInteractionListener mListener;

    private GoogleMap googleMap;

    public LogSpotFragment() {
        // Required empty public constructor
    }

    // The view items
    @BindView(R.id.etTrainId) EditText etTrainId;
    @BindView(R.id.tvDate) TextView tvDate;
    @BindView(R.id.mapHolder) FrameLayout mapHolder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_log_spot, container, false);

        //  SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
        //          .findFragmentById(R.id.mapHolder);
        //  ((LogSpotFragment)mapFragment).setMapFragment(mapFragment);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialise Butterknife
        ButterKnife.bind(this, view);
        // Set the date textview to today's date
        tvDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnTrainListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (googleMap != null) {
            googleMap.clear();

        }
        mapHolder.setVisibility(View.GONE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        Log.i("LSF", "Got map");
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

    }

    public void handleSave() {
        // Called when FAB is pressed in main activity and we're active
        if (tvDate.getText().length()==0 || etTrainId.getText().length()==0) {
            Toast.makeText(getContext(), "Fields where blank!\nNot saving", Toast.LENGTH_SHORT).show();
            return;
        }
        // TODO : Save location as well
        // Create and store object
        SightingDetails sightingDetails = new SightingDetails();
        sightingDetails.setTrainId(etTrainId.getText().toString());
        sightingDetails.setDate(tvDate.getText().toString());
        //sightingDetails.set
        // Store in the DB
        RealmHandler.getInstance().persistSightingDetails(sightingDetails);
        // Send to the server
        Submitter.getInstance().submitSighting(sightingDetails);
    }

    @OnClick(R.id.tvDate)
    void showCalendar() {
        CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                .setOnDateSetListener(new CalendarDatePickerDialogFragment.OnDateSetListener() {
                    @Override
                    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
                        tvDate.setText(dayOfMonth+"/"+(monthOfYear+1)+"/"+year);
                    }
                })
                .setFirstDayOfWeek(Calendar.SUNDAY);
                //.setPreselectedDate(towDaysAgo.getYear(), towDaysAgo.getMonthOfYear() - 1, towDaysAgo.getDayOfMonth())
                //.setDateRange(minDate, null)
                //.setDoneText("Yay")
                //.setCancelText("Nop")
                //.setThemeDark(true);
        cdp.show(getActivity().getSupportFragmentManager(), FRAG_TAG_DATE_PICKER);
    }
}
