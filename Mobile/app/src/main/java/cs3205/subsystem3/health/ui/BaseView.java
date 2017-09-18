package cs3205.subsystem3.health.ui;

/**
 * This is for fragment.
 * Created by Yee on 09/17/17.
 */

public interface BaseView<T extends BasePresenter> {
    void setPresenter(T presenter);
}
