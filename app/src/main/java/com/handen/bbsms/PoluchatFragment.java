package com.handen.bbsms;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

//import com.handen.handenview.dummy.DummyContent.DummyItem;

//import android.support.v7.widget.RecyclerView;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class PoluchatFragment extends Fragment {
    int mode = 1; // режим: 0 -- по категориям, 1 -- по получателям, 2 -- по конкретному получателю, 3 -- все платежи с сортировкой по дате.
    LayoutInflater inflater = null;
    Date date;
    TextView monthTextView;
    String filter; // фильтр получателей
    ArrayList<Database.SMS> listSMS = new ArrayList<>();

    String[] mon = {"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
    private OnListFragmentInteractionListener mListener;
    static TableLayout table = null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PoluchatFragment() {
    }

    public static PoluchatFragment newInstance() {
        PoluchatFragment fragment = new PoluchatFragment();
        Bundle args = new Bundle();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
//            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }


    public static void bubbleSort(ArrayList<Database.SMS> arr) {

/*Внешний цикл каждый раз сокращает фрагмент массива,
      так как внутренний цикл каждый раз ставит в конец
      фрагмента максимальный элемент*/

        for (int i = arr.size() - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {

/*Сравниваем элементы попарно,
              если они имеют неправильный порядок,
              то меняем местами*/

                if (arr.get(j).summ < arr.get(j + 1).summ) {
                    Database.SMS tmp = arr.get(j);
                    arr.set(j, arr.get(j + 1));
                    arr.set(j + 1, tmp);
                }
            }
        }
    }


    private void switchTo(Date date) {

        ArrayList<Database.SMS> SMSs = MainActivity.getSmsList(date);
        listSMS = new ArrayList<>();

        vibrate();
        table.removeAllViews();
        table.invalidate();

        float rashod_byn = 0, ukrali = 0, rashod_usd = 0, rashod_eur = 0;
        ArrayList<Float> prihod = new ArrayList<>();

        for (int i = 0; i < SMSs.size(); i++) {
            if (i > 0 && SMSs.get(i).curr.equals("BYN") && SMSs.get(i - 1).curr.equals("BYN")) {
                float realRash = SMSs.get(i - 1).balance - SMSs.get(i).balance;
                float err = realRash - SMSs.get(i).summ;
                if (err < 0) {
                    if (err < -0.01)
                        prihod.add(-err);
                } else if (err > 0) {
                    ukrali += err;
                }
            }
            if (SMSs.get(i).curr.equals("BYN"))
                rashod_byn += SMSs.get(i).summ;
            else if (SMSs.get(i).curr.equals("EUR"))
                rashod_eur += SMSs.get(i).summ;
            else if (SMSs.get(i).curr.equals("USD"))
                rashod_usd += SMSs.get(i).summ;
        }

        switch (mode) {
            case 3: // все платежи с сортировкой по датам
                rashod_byn = rashod_eur = rashod_usd = 0;
                for (int i = 0; i < SMSs.size(); i++) {
                    Database.SMS sms1 = new Database.SMS(SMSs.get(i));
                    listSMS.add(sms1);
                    if (sms1.curr.equals("BYN"))
                        rashod_byn += sms1.summ;
                    else if (sms1.curr.equals("EUR"))
                        rashod_eur += sms1.summ;
                    else if (sms1.curr.equals("USD"))
                        rashod_usd += sms1.summ;
                }
                break;
            case 2: // платежи одному получателю
                rashod_byn = rashod_eur = rashod_usd = 0;
                for (int i = 0; i < SMSs.size(); i++) {
                    if (SMSs.get(i).receiver.equals(filter)) {
                        Database.SMS sms1 = new Database.SMS(SMSs.get(i));
                        listSMS.add(sms1);
                        if (sms1.curr.equals("BYN"))
                            rashod_byn += sms1.summ;
                        else if (sms1.curr.equals("EUR"))
                            rashod_eur += sms1.summ;
                        else if (sms1.curr.equals("USD"))
                            rashod_usd += sms1.summ;
                    }
                }
                break;
            default:
                // группировка по получателям
                for (int i = 0; i < SMSs.size(); i++) {
                    boolean found = false;
                    for (int j = 0; j < listSMS.size(); j++) {
                        if (listSMS.get(j).receiver.equals(SMSs.get(i).receiver) && listSMS.get(j).curr.equals(SMSs.get(i).curr)) {
                            found = true;
                            float fl = SMSs.get(i).summ;
                            listSMS.get(j).summ += fl;
//                    Log.i("!!", listSMS.get(j).receiver + "  " + FlogroupsSMS.get(j).summ.toString());
                            break;
                        }
                    }
                    if (!found) {
//                Log.i("!!", "+ " + SMSs.get(i).receiver + "  " + SMSs.get(i).summ.toString());
                        Database.SMS sms1 = new Database.SMS(SMSs.get(i));
                        listSMS.add(sms1);
                    }
                }

                // отсортируем по сумме
                bubbleSort(listSMS);
                break;
        }

        TableRow tr;
        TextView tv;

        int month = date.getMonth();
        if (month >= 0 && month < 12) {
            monthTextView.setText(mon[month] + " " + Integer.toString(date.getYear() + 1900));
            monthTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mode = 3;
                    updateView();
                }
            });

        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd");
        for (int i = 0; i < listSMS.size(); i++) {
            Database.SMS sms = listSMS.get(i);
            switch (mode) {
                case 2:
                    tr = (TableRow) inflater.inflate(R.layout.fragment_row_type2, null);
                    break;
                case 3:
                    tr = (TableRow) inflater.inflate(R.layout.fragment_row_type3, null);
                    break;
                default:
                    tr = (TableRow) inflater.inflate(R.layout.fragment_row_type1, null);
            }
            tv = (TextView) tr.findViewById(R.id.col1);
            if (mode == 2 || mode == 3)
                tv.setText(sdf.format(listSMS.get(i).date));
            tv = (TextView) tr.findViewById(R.id.col2);
            if (mode != 2)
                tv.setText(sms.receiver);
            else {
                String text = sms.receiver;
                String comment = Database.getComment(sms);
                if (comment.length() > 0) {
                    text = text + " (" + comment + ")";
                }
                tv.setText(text);
            }
            tv = (TextView) tr.findViewById(R.id.col3);
            tv.setText(new DecimalFormat("#0.00").format(sms.summ));
            tv = (TextView) tr.findViewById(R.id.col4);
            if (tv != null)
                tv.setText(sms.curr);
            if ((i % 2) == 0)
                tr.setBackgroundColor(ContextCompat.getColor(this.getActivity(), R.color.color_row1));
            else
                tr.setBackgroundColor(ContextCompat.getColor(this.getActivity(), R.color.color_row2));
            tr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("!!", "onClick");
                    if (mode == 1) {
                        mode = 2;
                        TableRow row = (TableRow) view;
                        if (table != null) {
                            for (int rowNum = 0; rowNum < table.getChildCount(); rowNum++) {
                                if (row == table.getChildAt(rowNum)) {
                                    String receiver = listSMS.get(rowNum).receiver;
                                    setFilter(receiver);
                                    break;
                                }
                            }
                        }
                    }
                    if (mode == 3 || mode == 2) {
//todo временно отключено                        commentPay(view);
                    }
                }
            });
            table.addView(tr); //добавляем созданную строку в таблицу
        }

        if (prihod.size() > 0 && mode != 2) {//TODO приходы опционально можно вывести на отдельном экране
            float pr = 0;
            for (int i = 0; i < prihod.size(); i++) {
                pr += prihod.get(i);
            }
            if (mode == 1)
                tr = (TableRow) inflater.inflate(R.layout.fragment_footer, null);
            else
                tr = (TableRow) inflater.inflate(R.layout.fragment_footer_4column, null);
            tv = tr.findViewById(R.id.col2);
            tv.setText("Приход:");
            tv.setGravity(Gravity.RIGHT);
            tv = tr.findViewById(R.id.col3);
            tv.setText(new DecimalFormat("#0.00").format(pr));
            tr.setBackgroundColor(ContextCompat.getColor(this.getActivity(), R.color.color_prihod));
            table.addView(tr); //добавляем созданную строку в таблицу
        }

        if (rashod_byn >= 0.01) {
            if (mode == 1)
                tr = (TableRow) inflater.inflate(R.layout.fragment_footer, null);
            else
                tr = (TableRow) inflater.inflate(R.layout.fragment_footer_4column, null);
            tv = tr.findViewById(R.id.col2);
            tv.setText("Расход:");
            tv.setGravity(Gravity.RIGHT);
            tv = tr.findViewById(R.id.col3);
            tv.setText(new DecimalFormat("#0.00").format(rashod_byn));
            tv = tr.findViewById(R.id.col4);
            tv.setText("BYN");
            tr.setBackgroundColor(ContextCompat.getColor(this.getActivity(), R.color.color_rashod));
            if (mode == 1 || mode == 2) {
                tr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mode = 3;
                        updateView();
                    }
                });
            }
            table.addView(tr); //добавляем созданную строку в таблицу
        }

        if (rashod_usd >= 0.01) {
            if (mode == 1)
                tr = (TableRow) inflater.inflate(R.layout.fragment_footer, null);
            else
                tr = (TableRow) inflater.inflate(R.layout.fragment_footer_4column, null);
            tv = tr.findViewById(R.id.col2);
            tv.setText("Расход:");
            tv.setGravity(Gravity.RIGHT);
            tv = tr.findViewById(R.id.col3);
            tv.setText(new DecimalFormat("#0.00").format(rashod_usd));
            tv = tr.findViewById(R.id.col4);
            tr.setBackgroundColor(ContextCompat.getColor(this.getActivity(), R.color.color_rashod));
            if (mode == 1) {
                tr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mode = 3;
                        updateView();
                    }
                });
            }
            table.addView(tr); //добавляем созданную строку в таблицу
        }

        if (rashod_eur >= 0.01) {
            if (mode == 1)
                tr = (TableRow) inflater.inflate(R.layout.fragment_footer, null);
            else
                tr = (TableRow) inflater.inflate(R.layout.fragment_footer_4column, null);
            tv = tr.findViewById(R.id.col2);
            tv.setText("Расход:");
            tv.setGravity(Gravity.RIGHT);
            tv = tr.findViewById(R.id.col3);
            tv.setText(new DecimalFormat("#0.00").format(rashod_eur));
            tv = tr.findViewById(R.id.col4);
            tv.setText("EUR");
            tr.setBackgroundColor(ContextCompat.getColor(this.getActivity(), R.color.color_rashod));
            if (mode == 1 || mode == 2) {
                tr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mode = 3;
                        updateView();
                    }
                });
            }
            table.addView(tr); //добавляем созданную строку в таблицу
        }

        if (ukrali >= 0.01 && mode != 2) {
            if (mode == 1)
                tr = (TableRow) inflater.inflate(R.layout.fragment_footer, null);
            else
                tr = (TableRow) inflater.inflate(R.layout.fragment_footer_4column, null);
            tv = tr.findViewById(R.id.col2);
            tv.setText("Расход без СМС:");
            tv.setGravity(Gravity.RIGHT);
            tv = tr.findViewById(R.id.col3);
            tv.setText(new DecimalFormat("#0.00").format(ukrali));
            tr.setBackgroundColor(ContextCompat.getColor(this.getActivity(), R.color.color_ukrali));
            table.addView(tr); //добавляем созданную строку в таблицу
        }

    }

    void setFilter(String _filter) {
        filter = _filter;
        switchTo(date);
    }

    void switchLeft() {
        if (date.getMonth() > 0)
            date.setMonth(date.getMonth() - 1);
        else
            date = new Date(date.getYear() - 1, 11, 1);
        switchTo(date);
    }

    void switchRight() {
        if (date.getMonth() < 11)
            date.setMonth(date.getMonth() + 1);
        else
            date = new Date(date.getYear() + 1, 0, 1);
        switchTo(date);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        this.inflater = inflater;
        View view = inflater.inflate(R.layout.fragment_poluchat_list, container, false);

        monthTextView = view.findViewById(R.id.month);
        table = (TableLayout) view.findViewById(R.id.table); // init table
        ImageButton button_left = view.findViewById(R.id.button_left);
        button_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchLeft();
            }
        });

        ImageButton button_right = view.findViewById(R.id.button_right);
        button_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchRight();
            }
        });

        if (date == null) {
            date = new Date();
            date = new Date(date.getYear(), date.getMonth(), 1);
        }
        switchTo(date);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnListFragmentInteractionListener {
//        void onListFragmentInteraction(DummyItem item);
    }

    void vibrate() {
        if (getActivity() != null) {
            MainActivity ma = (MainActivity) getActivity();
            ma.vibrate();
        }
    }

    void onBackPressed() {
        switch (mode) {
            case 2:
            case 3:
                mode = 1;
                switchTo(date);
                break;
        }
    }

    void updateView() {
        switchTo(date);
    }

    void commentPay(View v) {
        int rowNum = -1;
        for (int i = 0; i < table.getChildCount(); i++) {
            TableRow tr = (TableRow) table.getChildAt(i);
            if (tr == v) {
                rowNum = i;
                break;
            }
        }

        if (rowNum >= 0 && rowNum < listSMS.size()) {
            if (getActivity() != null) {
                MainActivity ma = (MainActivity) getActivity();
                ma.fragmentComment(listSMS.get(rowNum));
                updateView();
            }
        }
    }

    void renameReceiver(View v) {
/*
        int rowNum = -1;
        for (int i = 0; i < table.getChildCount(); i++) {
            TableRow tr = (TableRow) table.getChildAt(i);
            if (tr.findViewById(R.id.settings) == v) {
                rowNum = i;
                break;
            }
        }

        if (rowNum >= 0 && rowNum < listSMS.size()) {
            if (getActivity() != null) {
                MainActivity ma = (MainActivity) getActivity();
                ma.fragmentRename(listSMS.get(rowNum).receiver);
                updateView();
            }
        }
*/
    }
}
