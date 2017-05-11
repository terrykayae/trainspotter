package uk.co.tezk.trainspotter.presenter;

import java.util.List;

/**
 * Created by tezk on 11/05/17.
 */

public interface IClassListPresenter extends BasePresenter {
    public interface IView extends BasePresenter.IView {
        public void showClassList(List<String>classList) ;

    }

    public interface IPresenter extends BasePresenter.IPresenter {
        public void bind(IView view) ;
    }
}
