package uk.co.tezk.trainspotter.classList;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import javax.inject.Inject;

import uk.co.tezk.trainspotter.R;
import uk.co.tezk.trainspotter.TrainSpotterApplication;
import uk.co.tezk.trainspotter.base.TrainspotterDialogSupport;
import uk.co.tezk.trainspotter.model.ClassDetails;

public class ClassListFragment extends Fragment implements ClassListContract.View, ClassListRecyclerViewAdapter.OnClassListItemClickListener {
    // Holder for the listener activity that we call
    private OnClassListFragmentInteractionListener mListener;

    // Holder to access trainspotterDialog in MainActivity
    private TrainspotterDialogSupport trainspotterDialog;

    @Inject
    public ClassListContract.Presenter presenter;

    // Our list of classes for display
    private List<ClassDetails> classDetailsList;
    RecyclerView recyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ClassListFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TrainSpotterApplication.getApplication().getPresenterComponent().inject(this);
        presenter.bind(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnClassListFragmentInteractionListener) {
            mListener = (OnClassListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnClassListFragmentInteractionListener");
        }
        if (context instanceof TrainspotterDialogSupport)
            trainspotterDialog = (TrainspotterDialogSupport)context;
    }

    @Override
    public android.view.View onCreateView(LayoutInflater inflater,
                                          ViewGroup container,
                                          Bundle savedInstanceState) {
        android.view.View view = inflater.inflate(R.layout.fragment_class_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.classListRecyclerview);
        // Set the adapter
        Context context = view.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        return view;
    }


    @Override
    public void onViewCreated(android.view.View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.retrieveData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.unbind();
     //   mListener = null;

    }

    @Override
    public void onStartLoading() {
        trainspotterDialog.showProgressDialog();
    }

    @Override
    public void onErrorLoading(String message) {
      //  trainspotterDialog.dismiss();
        trainspotterDialog.showErrorMessage(message);
    }

    @Override
    public void onCompletedLoading() {
        trainspotterDialog.hideProgressDialog();
    }

    @Override
    public void showClassList(List<ClassDetails> classList) {
        // TODO : Convert these to items?
        recyclerView.setAdapter(new ClassListRecyclerViewAdapter(classList, this, this.getContext()));
    }

    // To allow the ViewHolder in the RecyclerView adapter to send us click events
    @Override
    public void onItemClick(String classId, boolean longClick) {
        mListener.onDisplayTrainsInClass(classId);
    }

 public interface OnClassListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onDisplayTrainsInClass(String classNum);
    }


}
