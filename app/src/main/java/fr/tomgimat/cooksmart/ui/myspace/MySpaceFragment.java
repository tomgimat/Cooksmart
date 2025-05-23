package fr.tomgimat.cooksmart.ui.myspace;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import fr.tomgimat.cooksmart.databinding.FragmentMySpaceBinding;

public class MySpaceFragment extends Fragment {

    private FragmentMySpaceBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MySpaceViewModel mySpaceViewModel =
                new ViewModelProvider(this).get(MySpaceViewModel.class);

        binding = FragmentMySpaceBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textMySpace;
        mySpaceViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}