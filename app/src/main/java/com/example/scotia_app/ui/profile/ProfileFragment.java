package com.example.scotia_app.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.scotia_app.R;
import com.example.scotia_app.User;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        Bundle bundle = getArguments();

        // Sets the textview's text to the user's name if the user is not null
        if (bundle != null) {
            User user = getArguments().getParcelable("user");
            final TextView textView = root.findViewById(R.id.text_profile);
            assert user != null;
            textView.append("Hello, " + user.getName() + "!");
        }

        return root;
    }

}