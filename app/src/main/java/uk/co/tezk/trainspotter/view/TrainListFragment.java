package uk.co.tezk.trainspotter.view;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.tezk.trainspotter.R;
import uk.co.tezk.trainspotter.adapters.TrainListRecyclerViewAdapter;
import uk.co.tezk.trainspotter.interfaces.TrainspotterDialogSupport;
import uk.co.tezk.trainspotter.model.Constant;
import uk.co.tezk.trainspotter.model.TrainDetail;
import uk.co.tezk.trainspotter.model.TrainListItem;
import uk.co.tezk.trainspotter.presenter.ITrainListPresenter;
import uk.co.tezk.trainspotter.presenter.TrainListPresenterImpl;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnTrainListFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class TrainListFragment extends Fragment implements
        ITrainListPresenter.IView,
        TrainListRecyclerViewAdapter.OnTrainListItemClickListener {

    private ITrainListPresenter.IPresenter presenter;

    private OnTrainListFragmentInteractionListener mListener;

    // Holder to access progressDialog in MainActivity
    private TrainspotterDialogSupport progressDialog;

    List <TrainListItem> mTrainList;
    private String showTrainsForClass;

    @BindView(R.id.trainListRecyclerView) RecyclerView trainListRecyclerView;

    public String getShowTrainsForClass() {
        return showTrainsForClass;
    }

    public void setShowTrainsForClass(String showTrainsForClass) {
        if (showTrainsForClass!=null)
            this.showTrainsForClass = showTrainsForClass;
        else
            this.showTrainsForClass = "1";
        Log.i("TLF", "Setting class to "+showTrainsForClass);
    }

    public TrainListFragment() {
        // Required empty public constructor
        mTrainList = new ArrayList();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTrainListFragmentInteractionListener) {
            mListener = (OnTrainListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnTrainListFragmentInteractionListener");
        }
        if (context instanceof TrainspotterDialogSupport)
            progressDialog = (TrainspotterDialogSupport)context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // bug on later APIs
        super.onCreate(savedInstanceState);
        if (savedInstanceState!=null && savedInstanceState.getString(Constant.CLASS_NUM_KEY)!=null) {
            setShowTrainsForClass(savedInstanceState.getString(Constant.CLASS_NUM_KEY));
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        presenter = new TrainListPresenterImpl();
        presenter.bind(this);

       // Inflate the layout for this fragment
        int layout = R.layout.fragment_train_list;
        return inflater.inflate(layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialise Butterknife anotations
        ButterKnife.bind(this, view);

        trainListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Log.i("TLF", "Get trains for "+showTrainsForClass);
        reloadTrainList();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save the current class we're viewing
        outState.putString(Constant.CLASS_NUM_KEY, showTrainsForClass);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        //restore the class we were viewing
        if (savedInstanceState!=null && savedInstanceState.getString(Constant.CLASS_NUM_KEY)!=null) {
            setShowTrainsForClass(savedInstanceState.getString(Constant.CLASS_NUM_KEY));
            reloadTrainList();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.unbind();
    }

    public void reloadTrainList() {
        Log.i("TLF", "reloadTrainList : "+showTrainsForClass);
        if (mTrainList.size()==0) {
            if (showTrainsForClass==null || showTrainsForClass.equals("0")) {
                return;
            }
            if (presenter == null) {
                presenter = new TrainListPresenterImpl();
                presenter.bind(this);
            }
            presenter.retrieveData(showTrainsForClass);
        }
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            // TODO : need to pass train details from selected item
            mListener.onShowTrainDetails(uri.toString(), uri.toString());
        }
    }

    public void searchFor(String searchString) {
        if (presenter == null) {
            presenter = new TrainListPresenterImpl();
            presenter.bind(this);
        }
        presenter.performSearch(searchString);
    }

    @Override
    public void showTrainList(List<TrainDetail> trainList) {
        Collections.sort(trainList, new Comparator<TrainDetail>() {
            @Override
            public int compare(TrainDetail o1, TrainDetail o2) {
                return o1.getTrain().getNumber().compareTo(o2.getTrain().getNumber());
            }
        });
        trainListRecyclerView.setAdapter(new TrainListRecyclerViewAdapter(trainList, this, getContext()));
    }

    @Override
    public void onStartLoading() {
        progressDialog.startProgressDialog();
    }

    @Override
    public void onErrorLoading(String message) {
//        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
  //      progressDialog.stopProgressDialog();
        Log.i("TLF", "onErrorLoading = "+message);
    }

    @Override
    public void onCompletedLoading() {
        progressDialog.stopProgressDialog();
    }

    @Override
    public void onItemClick(String classId, String trainNum, boolean longClick) {
        // Received from the Adapter, pass to the Activity for actioning
        mListener.onShowTrainDetails(classId, trainNum);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnTrainListFragmentInteractionListener {
        void onShowTrainDetails(String classNum, String trainNum) ;
    }
}
