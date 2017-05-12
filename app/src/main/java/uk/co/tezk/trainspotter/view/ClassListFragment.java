package uk.co.tezk.trainspotter.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import uk.co.tezk.trainspotter.R;
import uk.co.tezk.trainspotter.presenter.ClassListPresenterImpl;
import uk.co.tezk.trainspotter.presenter.IClassListPresenter;
import uk.co.tezk.trainspotter.view.dummy.DummyContent;
import uk.co.tezk.trainspotter.view.dummy.DummyContent.DummyItem;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnClassListFragmentInteractionListener}
 * interface.
 */
public class ClassListFragment extends Fragment implements IClassListPresenter.IView {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnClassListFragmentInteractionListener mListener;

    private Dialog progressDialog;
    private IClassListPresenter.IPresenter presenter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ClassListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ClassListFragment newInstance(int columnCount) {
        ClassListFragment fragment = new ClassListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("CLF", "oncreate");

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        progressDialog = new Dialog(getActivity());
        progressDialog.setTitle("Getting data, please wait");

        presenter = ClassListPresenterImpl.getInstance();
        presenter.bind(this);
        presenter.retrieveData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class_list, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.classListRecyclerview);
        Log.i("CLF", "onCreateView");
        // Set the adapter
        if (recyclerView instanceof RecyclerView) {
            Context context = view.getContext();
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new ClassTotalRecyclerViewAdapter(DummyContent.ITEMS, mListener));
        }
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
        progressDialog.dismiss();
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCompletedLoading() {
        //progressDialog.dismiss();
    }

    @Override
    public void showClassList(List<String> classList) {
        Log.i("CLF","list contains "+classList.size());
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
        void onListFragmentInteraction(DummyItem item);
    }
}
