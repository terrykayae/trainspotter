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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.tezk.trainspotter.R;
import uk.co.tezk.trainspotter.adapters.TrainListRecyclerViewAdapter;
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

    List <TrainListItem> mTrainList;
    private String showTrainsForClass;

    private boolean forcePortrait;

    @BindView(R.id.trainListRecyclerView) RecyclerView trainListRecyclerView;

    public String getShowTrainsForClass() {
        return showTrainsForClass;
    }

    public void setShowTrainsForClass(String showTrainsForClass) {
        this.showTrainsForClass = showTrainsForClass;
        Log.i("TLF", "Setting class to "+showTrainsForClass);
    }

    public TrainListFragment() {
        // Required empty public constructor
        mTrainList = new ArrayList();
    }

    public void forcePortrait() {
        // As we use different layouts, call this to force loading of a portrait layout
        forcePortrait = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        presenter = new TrainListPresenterImpl();
        presenter.bind(this);
       // Inflate the layout for this fragment
        int layout = forcePortrait?R.layout.fragment_train_list_subfragment:R.layout.fragment_train_list;
        return inflater.inflate(layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialise Butterknife anotations
        ButterKnife.bind(this, view);
        trainListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Log.i("TLF", "Get trains for "+showTrainsForClass);
       // reloadTrainList();
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadTrainList();
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
            Log.i("TLF", "rltl, calling retrieveData "+showTrainsForClass);
            presenter.retrieveData(showTrainsForClass);
        } else {
            Log.i("TLF", "rltl, mTrainList.size = "+mTrainList.size());
        }
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            // TODO : need to pass train details from selected item
            mListener.onShowTrainDetails(uri.toString(), uri.toString());
        }
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
        Log.i("TLF", "Loading data");
    }

    @Override
    public void onErrorLoading(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCompletedLoading() {
        Log.i("TLF", "Completed loading");
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnTrainListFragmentInteractionListener {
        void onShowTrainDetails(String classNum, String trainNum) ;
    }
}
