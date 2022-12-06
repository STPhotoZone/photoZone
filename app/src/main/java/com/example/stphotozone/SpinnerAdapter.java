package com.example.stphotozone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class SpinnerAdapter extends BaseAdapter {

    private final List<String> list;
    private final LayoutInflater inflater;
    private String text;
    public static boolean flag = false;


    public SpinnerAdapter(Context context, List<String> list1) {
        this.list = list1;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() { // 즉 spinner에 들어갈 text 개수 count
        if(list != null) return list.size();
        else return 0;
    }

    @Override
    public Object getItem(int i) {
        return list.get(i); //해당 item을 선택
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    // 화면에 있는 스피너 뷰 설정
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null)
            view = inflater.inflate(R.layout.spinner_outer_view, viewGroup, false);
        if(flag){
            text = list.get(i);
            ((TextView) view.findViewById(R.id.inner_text)).setText(text); // 즉 밖에서 보여지는 text 설정
        }
        else{
            // 아무것도 선택 ㄴㄴ
            ((TextView) view.findViewById(R.id.inner_text)).setText(" ");
        }
        return view;
    }

    //클릭 후 나타나는 텍스트 뷰 설정
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.spinner_item, parent, false);
        if (list != null) {
            text = list.get(position);
            ((TextView) convertView.findViewById(R.id.spinner_text)).setText(text);
        }

        return convertView;
    }

    // 스피너에서 선택된 아이템을 액티비티에서 꺼내오는 메서드
    public String getItem() {
        return text;
    }

}
