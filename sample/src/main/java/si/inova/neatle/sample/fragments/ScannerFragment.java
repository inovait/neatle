package si.inova.neatle.sample.fragments;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import si.inova.neatle.Neatle;
import si.inova.neatle.sample.R;
import si.inova.neatle.scan.ScanEvent;
import si.inova.neatle.scan.Scanner;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnDeviceSelected}
 * interface.
 */
public class ScannerFragment extends Fragment implements Scanner.ScanEventListener, Scanner.NewDeviceFoundListener {
    private OnDeviceSelected listener;
    private ScannerRecyclerViewAdapter adapter;
    private Scanner scanner;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ScannerFragment() {
    }

    public static ScannerFragment newInstance() {
        ScannerFragment fragment = new ScannerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scanner = Neatle.createScannerBuilder()
                .setNewDeviceFoundListener(this)
                //.setScanEventListener(this)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView view = (RecyclerView) inflater.inflate(R.layout.fragment_scanner_list, container, false);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL);
        view.addItemDecoration(dividerItemDecoration);

        adapter = new ScannerRecyclerViewAdapter(listener);
        view.setAdapter(adapter);

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDeviceSelected) {
            listener = (OnDeviceSelected) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDeviceSelected");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        scanner.startScanning(getContext());
    }



    public void onNewDeviceFound(ScanEvent e) {
        adapter.onScanEvent(e);
    }
    @Override
    public void onScanEvent(ScanEvent e) {
        //adapter.onScanEvent(e);
    }

    @Override
    public void onPause() {
        super.onPause();

        scanner.stopScanning();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }


    public interface OnDeviceSelected {
        void onDeviceSelected(BluetoothDevice item);
    }

    /**
     * {@link RecyclerView.Adapter} that can display a {@link FoundDevice} and makes a call to the
     * specified {@link OnDeviceSelected}.
     * TODO: Replace the implementation with code for your data type.
     */
    public static class ScannerRecyclerViewAdapter extends RecyclerView.Adapter<ScannerRecyclerViewAdapter.ViewHolder> {

        private final List<ScanEvent> mValues = new ArrayList<>();
        private final OnDeviceSelected mListener;

        public ScannerRecyclerViewAdapter(OnDeviceSelected listener) {
            mListener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_scanner, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);

            ScanEvent e = mValues.get(position);
            BluetoothDevice device = e.getDevice();
            String name = device.getName();
            if (name == null || name.length() == 0) {
                name = "No name";
            }

            holder.mIdView.setText(name);
            holder.mContentView.setText("MAC: " + device.getAddress() + ", rssi:" + e.getRssi());

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.
                        mListener.onDeviceSelected(holder.mItem.getDevice());
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }



        public void onScanEvent(ScanEvent e) {
            boolean exists = false;
            for (int i = 0; i < mValues.size(); i++ ) {
                ScanEvent tmp = mValues.get(i);
                if (tmp.getDevice().equals(e.getDevice())) {
                    exists = true;
                    mValues.set(i, e);
                    break;
                }
            }
            if (!exists) {
                mValues.add(e);
            }
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public ScanEvent mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);

            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
