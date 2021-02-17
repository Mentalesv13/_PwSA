package com.example.scanandgo_pwsa.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.scanandgo_pwsa.MainActivity;
import com.example.scanandgo_pwsa.R;
import com.example.scanandgo_pwsa.helper.LoadingDialog;
import com.example.scanandgo_pwsa.helper.RomanNumber;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpensesChart extends Fragment {
    private ConstraintLayout clEmpty;
    private GraphView graph, graph2;
    private FirebaseFirestore db;
    private LoadingDialog loadingDialog;
    private FirebaseAuth mAuth;
    private TextView chartTitle;
    private boolean empty;
    private int selected = 2;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_expanses_chart, container, false);
        FloatingActionButton btnBack = null;
        loadingDialog = new LoadingDialog(getActivity());
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        empty = true;
        chartTitle = view.findViewById(R.id.chartTitle);
        chartTitle.setVisibility(View.GONE);
        clEmpty = view.findViewById(R.id.emptyTransaction);
        clEmpty.setVisibility(View.GONE);
        btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).setFragment(new Profile());
            }
        });

        graph = (GraphView) view.findViewById(R.id.graph);
        graph.setVisibility(View.GONE);
        graph2 = (GraphView) view.findViewById(R.id.graph2);
        graph.setVisibility(View.GONE);

        loadData();

        return view;
    }

    public void loadData()
    {
        DocumentReference docRef = db.collection("bills").document(mAuth.getCurrentUser().getUid().trim());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                HashMap<String, List<Double>> daylies = new HashMap<>();
                List<Double> days = new ArrayList<>();
                for (int i = 0; i <= 32; i++) {
                    days.add(0.0);
                }

                HashMap<String, Double> temporary = new HashMap<>();
                temporary.put("I", 0.0);
                temporary.put("II", 0.0);
                temporary.put("III", 0.0);
                temporary.put("IV", 0.0);
                temporary.put("V", 0.0);
                temporary.put("VI", 0.0);
                temporary.put("VII", 0.0);
                temporary.put("VIII", 0.0);
                temporary.put("IX", 0.0);
                temporary.put("X", 0.0);
                temporary.put("XI", 0.0);
                temporary.put("XII", 0.0);
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //Log.e("TAG", document.getData().toString());
                        List<Map> categories = (List<Map>) document.get("bill");
                        if (categories.size() > 0) {
                            empty = false;
                            clEmpty.setVisibility(View.GONE);
                            graph.setVisibility(View.VISIBLE);
                            graph2.setVisibility(View.VISIBLE);
                            chartTitle.setVisibility(View.VISIBLE);
                            Calendar c = Calendar.getInstance();
                            boolean flag = false;
                            String monthValue = "";
                            for (int i = categories.size() - 1; i >= 0; i--) {
                                Map<String, Object> bill = categories.get(i);
                                String date = bill.get("date").toString();
                                String day = date.substring(8, 10);
                                //Log.e("TAG", day);
                                date = date.substring(5, 7);
                                int month = Integer.parseInt(date);
                                int dayInt = Integer.parseInt(day);
                                if (month == 1)
                                {
                                    temporary.put("I", temporary.get("I") + (Double) bill.get("total"));
                                }
                                else if (month == 2)
                                {
                                    temporary.put("II", temporary.get("II") + (Double) bill.get("total"));
                                }
                                else if (month == 3)
                                {
                                    temporary.put("III", temporary.get("III") + (Double) bill.get("total"));
                                }
                                else if (month == 4)
                                {
                                    temporary.put("IV", temporary.get("IV") + (Double) bill.get("total"));
                                }
                                else if (month == 5)
                                {
                                    temporary.put("V", temporary.get("V") + (Double) bill.get("total"));
                                }
                                else if (month == 6)
                                {
                                    temporary.put("VI", temporary.get("VI") + (Double) bill.get("total"));
                                }
                                else if (month == 7)
                                {
                                    temporary.put("VII", temporary.get("VII") + (Double) bill.get("total"));
                                }
                                else if (month == 8)
                                {
                                    temporary.put("VIII", temporary.get("VIII") + (Double) bill.get("total"));
                                }
                                else if (month == 9)
                                {
                                    temporary.put("IX", temporary.get("IX") + (Double) bill.get("total"));
                                }
                                else if (month == 10)
                                {
                                    temporary.put("X", temporary.get("X") + (Double) bill.get("total"));
                                }
                                else if (month == 11)
                                {
                                    temporary.put("XI", temporary.get("XI") + (Double) bill.get("total"));
                                }
                                else if (month == 12)
                                {
                                    temporary.put("XII", temporary.get("XII") + (Double) bill.get("total"));
                                }

                                if (month == c.MONTH)

                                {
                                    //daylies.put("II", days);
                                    if(!flag) {
                                        days = new ArrayList<>();
                                        for (int k = 0; k <= 32; k++) {
                                            days.add(0.0);
                                        }
                                    }
                                    days.set(dayInt, days.get(dayInt) + (Double) bill.get("total"));
                                    flag = true;
                                    monthValue = RomanNumber.toRoman(month);
                                    Log.e("TAG",monthValue);
                                }

                            }
//                            if(!flag)
//                            {
//                                daylies.put("I", days);}
//                            else
                            daylies.put(monthValue, days);
                            graph2.setVisibility(View.GONE);
                        } else {
                            clEmpty.setVisibility(View.VISIBLE);
                            graph.setVisibility(View.GONE);
                            graph2.setVisibility(View.GONE);
                            chartTitle.setVisibility(View.GONE);
                            empty = true;
                        }
                    }

                }
                if (!empty) {
                    LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[]{
                            new DataPoint(0, temporary.get("I")),
                            new DataPoint(1, temporary.get("II")),
                            new DataPoint(2, temporary.get("III")),
                            new DataPoint(3, temporary.get("IV")),
                            new DataPoint(4, temporary.get("V")),
                            new DataPoint(5, temporary.get("VI")),
                            new DataPoint(6, temporary.get("VII")),
                            new DataPoint(7, temporary.get("VIII")),
                            new DataPoint(8, temporary.get("IX")),
                            new DataPoint(9, temporary.get("X")),
                            new DataPoint(10, temporary.get("XI")),
                            new DataPoint(11, temporary.get("XII")),
                    });

                    series.setOnDataPointTapListener(new OnDataPointTapListener() {
                        @Override
                        public void onTap(Series series, final DataPointInterface dataPoint) {

                            if ((dataPoint.getX() >= 0 && dataPoint.getX() <= 11 )&& dataPoint.getY()>0.0) {
                                DecimalFormatSymbols symbol = new DecimalFormatSymbols(Locale.US);
                                DecimalFormat decfs = new DecimalFormat("#0.00", symbol);
                                if(Locale.getDefault().getDisplayLanguage().equals("polski"))
                                    Toast.makeText(getContext(), "Wydatki w tym miesiącu: " + decfs.format(dataPoint.getY()) + " zł", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(getContext(), "Expenses this month: " + decfs.format(dataPoint.getY()) + " zł", Toast.LENGTH_SHORT).show();
                                if (selected != (int) dataPoint.getX()) {
                                    selected = (int) dataPoint.getX();
                                    showCustomLoadingDialog();
                                    graph2.removeAllSeries();
                                    if (dataPoint.getX() == 0) {
                                        chartTitle.setText(R.string.January);
                                    } else if (dataPoint.getX() == 1) {
                                        chartTitle.setText(R.string.February);
                                    } else if (dataPoint.getX() == 2) {
                                        chartTitle.setText(R.string.March);
                                    } else if (dataPoint.getX() == 3) {
                                        chartTitle.setText(R.string.April);
                                    } else if (dataPoint.getX() == 4) {
                                        chartTitle.setText(R.string.May);
                                    } else if (dataPoint.getX() == 5) {
                                        chartTitle.setText(R.string.June);
                                    } else if (dataPoint.getX() == 6) {
                                        chartTitle.setText(R.string.July);
                                    } else if (dataPoint.getX() == 7) {
                                        chartTitle.setText(R.string.August);
                                    } else if (dataPoint.getX() == 8) {
                                        chartTitle.setText(R.string.September);
                                    } else if (dataPoint.getX() == 9) {
                                        chartTitle.setText(R.string.October);
                                    } else if (dataPoint.getX() == 10) {
                                        chartTitle.setText(R.string.November);
                                    } else if (dataPoint.getX() == 11) {
                                        chartTitle.setText(R.string.December);
                                    }

                                    DocumentReference documentReference = db.collection("bills").document(mAuth.getCurrentUser().getUid().trim());
                                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            List<Double> days = new ArrayList<>();
                                            for (int i = 0; i <= 32; i++) {
                                                days.add(0.0);
                                            }
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    //Log.e("TAG", document.getData().toString());
                                                    List<Map> categories = (List<Map>) document.get("bill");
                                                    if (categories.size() > 0) {
                                                        clEmpty.setVisibility(View.GONE);
                                                        graph.setVisibility(View.VISIBLE);
                                                        graph2.setVisibility(View.VISIBLE);
                                                        chartTitle.setVisibility(View.VISIBLE);
                                                        boolean flag = false;
                                                        for (int i = categories.size() - 1; i >= 0; i--) {
                                                            Map<String, Object> bill = categories.get(i);
                                                            String date = bill.get("date").toString();
                                                            String day = date.substring(8, 10);
                                                            //Log.e("TAG", day);
                                                            date = date.substring(5, 7);
                                                            int month = Integer.parseInt(date);
                                                            if (dataPoint.getX() == 0) {
                                                                if (month == 1) {
                                                                    int dayInt = Integer.parseInt(day);
                                                                    days.set(dayInt, days.get(dayInt) + (Double) bill.get("total"));
                                                                }
                                                            } else if (dataPoint.getX() == 1) {
                                                                if (month == 2) {
                                                                    int dayInt = Integer.parseInt(day);
                                                                    days.set(dayInt, days.get(dayInt) + (Double) bill.get("total"));
                                                                }
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    clEmpty.setVisibility(View.VISIBLE);
                                                    graph.setVisibility(View.GONE);
                                                    graph2.setVisibility(View.GONE);
                                                    chartTitle.setVisibility(View.GONE);
                                                }
                                            }

                                            LineGraphSeries<DataPoint> series1 = new LineGraphSeries<DataPoint>(new DataPoint[]{
                                                    new DataPoint(0, days.get(1)),
                                                    new DataPoint(1, days.get(2)),
                                                    new DataPoint(2, days.get(3)),
                                                    new DataPoint(3, days.get(4)),
                                                    new DataPoint(4, days.get(5)),
                                                    new DataPoint(5, days.get(6)),
                                                    new DataPoint(6, days.get(7)),
                                                    new DataPoint(7, days.get(8)),
                                                    new DataPoint(8, days.get(9)),
                                                    new DataPoint(9, days.get(10)),
                                                    new DataPoint(10, days.get(11)),
                                                    new DataPoint(11, days.get(12)),
                                                    new DataPoint(12, days.get(13)),
                                                    new DataPoint(13, days.get(14)),
                                                    new DataPoint(14, days.get(15)),
                                                    new DataPoint(15, days.get(16)),
                                                    new DataPoint(16, days.get(17)),
                                                    new DataPoint(17, days.get(18)),
                                                    new DataPoint(18, days.get(19)),
                                                    new DataPoint(19, days.get(20)),
                                                    new DataPoint(20, days.get(21)),
                                                    new DataPoint(21, days.get(22)),
                                                    new DataPoint(22, days.get(23)),
                                                    new DataPoint(23, days.get(24)),
                                                    new DataPoint(24, days.get(25)),
                                                    new DataPoint(25, days.get(26)),
                                                    new DataPoint(26, days.get(27)),
                                                    new DataPoint(27, days.get(28)),
                                                    new DataPoint(28, days.get(29)),
                                                    new DataPoint(29, days.get(30)),
                                                    new DataPoint(30, days.get(31)),
                                            });

                                            series1.setOnDataPointTapListener(new OnDataPointTapListener() {
                                                @Override
                                                public void onTap(Series series, DataPointInterface dataPoint) {
                                                    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                                                    DecimalFormat decf = new DecimalFormat("#0", symbols);
                                                    DecimalFormatSymbols symbol = new DecimalFormatSymbols(Locale.US);
                                                    DecimalFormat decfs = new DecimalFormat("#0.00", symbol);
                                                    if(Locale.getDefault().getDisplayLanguage().equals("polski"))
                                                    Toast.makeText(getContext(), "Wydatki " + decf.format(dataPoint.getX() + 1.0) + ".: " + decfs.format(dataPoint.getY()) + " zł", Toast.LENGTH_SHORT).show();
                                                    else
                                                        Toast.makeText(getContext(), "Expenses at " + decf.format(dataPoint.getX() + 1.0) + ".: " + decfs.format(dataPoint.getY()) + " zł", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            series1.setDrawDataPoints(true);
                                            StaticLabelsFormatter staticLabelsFormatter1 = new StaticLabelsFormatter(graph2);
                                            staticLabelsFormatter1.setHorizontalLabels(new String[]{"1", "6", "11",
                                                    "16", "21", "26", "31"});
                                            graph2.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter1);
                                            graph2.addSeries(series1);
                                        }
                                    });
                                }
                            }
                        }
                    });
                    series.setDrawDataPoints(true);
                    StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
                    staticLabelsFormatter.setHorizontalLabels(new String[]{"I","II","III","IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII"});
                    graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
                    graph.addSeries(series);

                    List<Double> tempList = daylies.get("II");

                    LineGraphSeries<DataPoint> series1 = new LineGraphSeries<DataPoint>(new DataPoint[]{

                            new DataPoint(0, tempList.get(1)),
                            new DataPoint(1, tempList.get(2)),
                            new DataPoint(2, tempList.get(3)),
                            new DataPoint(3, tempList.get(4)),
                            new DataPoint(4, tempList.get(5)),
                            new DataPoint(5, tempList.get(6)),
                            new DataPoint(6, tempList.get(7)),
                            new DataPoint(7, tempList.get(8)),
                            new DataPoint(8, tempList.get(9)),
                            new DataPoint(9, tempList.get(10)),
                            new DataPoint(10, tempList.get(11)),
                            new DataPoint(11, tempList.get(12)),
                            new DataPoint(12, tempList.get(13)),
                            new DataPoint(13, tempList.get(14)),
                            new DataPoint(14, tempList.get(15)),
                            new DataPoint(15, tempList.get(16)),
                            new DataPoint(16, tempList.get(17)),
                            new DataPoint(17, tempList.get(18)),
                            new DataPoint(18, tempList.get(19)),
                            new DataPoint(19, tempList.get(20)),
                            new DataPoint(20, tempList.get(21)),
                            new DataPoint(21, tempList.get(22)),
                            new DataPoint(22, tempList.get(23)),
                            new DataPoint(23, tempList.get(24)),
                            new DataPoint(24, tempList.get(25)),
                            new DataPoint(25, tempList.get(26)),
                            new DataPoint(26, tempList.get(27)),
                            new DataPoint(27, tempList.get(28)),
                            new DataPoint(28, tempList.get(29)),
                            new DataPoint(29, tempList.get(30)),
                            new DataPoint(30, tempList.get(31)),
                    });

                    series1.setOnDataPointTapListener(new OnDataPointTapListener() {
                        @Override
                        public void onTap(Series series, DataPointInterface dataPoint) {
                            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                            DecimalFormat decf = new DecimalFormat("#0", symbols);
                            DecimalFormatSymbols symbol = new DecimalFormatSymbols(Locale.US);
                            DecimalFormat decfs = new DecimalFormat("#0.00", symbol);
                            if(Locale.getDefault().getDisplayLanguage().equals("polski"))
                                Toast.makeText(getContext(), "Wydatki " + decf.format(dataPoint.getX() + 1.0) + ".: " + decfs.format(dataPoint.getY()) + " zł", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getContext(), "Expenses at " + decf.format(dataPoint.getX() + 1.0) + ".: " + decfs.format(dataPoint.getY()) + " zł", Toast.LENGTH_SHORT).show();
                        }
                    });
                    series1.setDrawDataPoints(true);
                    StaticLabelsFormatter staticLabelsFormatter1 = new StaticLabelsFormatter(graph2);
                    staticLabelsFormatter1.setHorizontalLabels(new String[]{"1", "6", "11", "16", "21", "26", "31"});
                    graph2.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter1);
                    graph2.addSeries(series1);
                }
                else {
                    clEmpty.setVisibility(View.VISIBLE);
                    graph.setVisibility(View.GONE);
                    graph2.setVisibility(View.GONE);
                    chartTitle.setVisibility(View.GONE);
                    empty = true;
                }
            }
        });

    }

    private void showCustomLoadingDialog() {
        graph.setAlpha(0.10f);
        graph2.setAlpha(0.0f);
        chartTitle.setAlpha(0.0f);
        loadingDialog.showDialog();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.hideDialog();
                graph.setAlpha(1f);
                graph2.setAlpha(1f);
                chartTitle.setAlpha(1f);
            }
        }, 750);
    }
}
