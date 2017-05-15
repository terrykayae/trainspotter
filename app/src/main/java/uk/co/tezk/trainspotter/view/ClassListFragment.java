package uk.co.tezk.trainspotter.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import uk.co.tezk.trainspotter.R;
import uk.co.tezk.trainspotter.model.ClassDetails;
import uk.co.tezk.trainspotter.presenter.ClassListPresenterImpl;
import uk.co.tezk.trainspotter.presenter.IClassListPresenter;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnClassListFragmentInteractionListener}
 * interface.
 */
public class ClassListFragment extends Fragment implements IClassListPresenter.IView, ClassListRecyclerViewAdapter.OnClassListItemClickListener {
    // Holder for the listener activity that we call
    private OnClassListFragmentInteractionListener mListener;

    private Dialog progressDialog;
    private IClassListPresenter.IPresenter presenter;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("CLF", "oncreate");

        progressDialog = new Dialog(getActivity());
        progressDialog.setTitle("Getting data, please wait");

        presenter = ClassListPresenterImpl.getInstance();
        presenter.bind(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.classListRecyclerview);
        Log.i("CLF", "onCreateView");
        // Set the adapter
        Context context = view.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        presenter.retrieveData();
        return view;
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStartLoading() {
        //progressDialog.show();
    }

    @Override
    public void onErrorLoading(String message) {
      //  progressDialog.dismiss();
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCompletedLoading() {
        progressDialog.dismiss();
    }

    @Override
    public void showClassList(List<String> classList) {
        Log.i("CLF", "list contains " + classList.size());
        // TODO : Convert these to items?
        classDetailsList = new ArrayList();
        for (String each : classList) {
            ClassDetails classDetails = new ClassDetails();
            classDetails.setClassId(each);
            classDetailsList.add(classDetails);
        }
        recyclerView.setAdapter(new ClassListRecyclerViewAdapter(classDetailsList, this, this.getContext()));
    }

    // To allow the ViewHolder in the RecyclerView adapter to send us click events
    @Override
    public void onItemClick(String classId, boolean longClick) {
        // TODO : Implement
        mListener.onDisplayTrainsInClass(classId);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnClassListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onDisplayTrainsInClass(String classNum);
    }


}
