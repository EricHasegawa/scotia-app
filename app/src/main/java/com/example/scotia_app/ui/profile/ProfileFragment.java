package com.example.scotia_app.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.scotia_app.R;
import com.example.scotia_app.data.model.User;

public class ProfileFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        // Sets the textview's text to the user's name if the user is not null
        Bundle bundle = getArguments();
        final TextView textView = root.findViewById(R.id.text_profile);
        if (bundle != null) {
            textView.clearComposingText();
            User user = bundle.getParcelable("user");
            assert user != null;
            textView.append("Hello, " + user.getName() + "!");
        }

        return root;
    }

}