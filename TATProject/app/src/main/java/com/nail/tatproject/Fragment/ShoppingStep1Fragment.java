package com.nail.tatproject.Fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nail.tatproject.MainActivity;
import com.nail.tatproject.R;
import com.nail.tatproject.moudle.Product;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.max;

/**
 * Created by 70J on 2016/6/17.
 */
public class ShoppingStep1Fragment extends Fragment {
    private RecyclerView listView;
    private TextView product_total,products_price,products_discount,products_total;
    private ArrayList<Product> products = new ArrayList<>();
    private int ID[]={1 ,4 , 10 , 45 , 25};
    private int ID_max = 5;
    private int sum = 0,discount = 0 ;
    private final String ARG_SECTION_NUMBER = "section_number";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("ShoppingStep1Fragment", "onCreate");
        setRetainInstance(true);
        products.clear();
        for(int i = 0 ; i < ID_max ;i++){
            new AsyncGetProduct().execute("http://tatvip.ezsale.tw/tat/api/getprod.ashx", String.valueOf(ID[i]));
        }
        /*LinearLayoutManager llm = new LinearLayoutManager(getView().this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);*/
    }

    @Override
    public void onHiddenChanged(boolean hidd) {
        if (hidd) {
            onPause();
        } else {
            onResume();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //導入Tab分頁的Fragment Layout
        Log.e("ShoppingStep1Fragment", "onCreateView");
        View view = inflater.inflate(R.layout.fragment_step1, container, false);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(false);
        listView = (RecyclerView) view.findViewById(R.id.listView_product);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        product_total = (TextView)view.findViewById(R.id.product_total);
        products_price = (TextView)view.findViewById(R.id.products_price);
        products_discount = (TextView)view.findViewById(R.id.products_discount);
        products_total = (TextView)view.findViewById(R.id.products_total);
        listView.setNestedScrollingEnabled(false);
        listView.setHasFixedSize(true);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("ShoppingStep1Fragment", "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("ShoppingStep1Fragment", "onPause");

    }

    public class Items {
        Items(int newid){
            ID = newid;
        }
        int ID = 1;
    }


    class AsyncGetProduct extends AsyncTask<String, Integer, Integer> {
        //================================================================
        String Reply;
        @Override
        protected Integer doInBackground(String... param) {
            //get Data 單存取資料
            Gson gson = new Gson();
            String content = null;
            try {
                content = "CheckM=" + URLEncoder.encode("286e5560eeac9d7ecb7ecbb6968148c7", "UTF-8");
                content +="&SiteID="+URLEncoder.encode("778", "UTF-8");
                content +="&Type="+URLEncoder.encode("4", "UTF-8");
                int id = Integer.valueOf(param[1]);
                content +="&Items="+gson.toJson(new Items(id));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return 0;
            }
            Reply = makeHttpRequest(param[0],"POST",content);
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            //此method是在doInBackground完成以後，才會呼叫的
            super.onPostExecute(result);
            if(result == 1){
                Log.d("result",Reply);
            try {
                //http://tatex.ezsale.tw/upload/1SP-OK-001(1).JPG
                JSONObject json_data = new JSONObject(Reply);
                Product module = new Product();
                module.id = json_data.optInt("ID");
                module.subid = json_data.optInt("SubID");
                module.image_URL = json_data.optString("Img1");
                //module.price = json_data.optInt("Value1");
                module.price = 1500;
                module.name = json_data.optString("Title");
                if(!json_data.isNull("Stock")) {
                    if (json_data.getJSONObject("Stock").has("Num")) {
                        module.product_max = json_data.getJSONObject("Stock").optInt("Num");
                    }
                    else module.product_max = 10;
                }
                else module.product_max = 10;
                module.type = "";
                module.count = 1;
                products.add(module);
                product_total.setText("共" + products.size() + "項商品");
                sum += (module.price * module.count );
                products_price.setText("$" + String.format("%,d",sum));
                products_discount.setText("-$" + String.format("%,d",discount));
                products_total.setText("$" + String.format("%,d",sum - discount));
                Log.d("Product", "ID=" + module.id +" SubID=" + module.subid +" URL=" + module.image_URL +" price=" + module.price+" count=" + module.count  + " max=" + module.product_max);
                ContactAdapter customAdapter = new ContactAdapter(products);
                listView.setAdapter(customAdapter);
                Log.d("Product_sum",products.size() + "");
            } catch (Exception e) {
                Log.e("FB JSON Parser", "Error parsing data " + e.toString());
                e.printStackTrace();
            }
            }
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        //=============================================================
    }

    public String makeHttpRequest(String temp_url, String method, String urlParameters){
        HttpURLConnection conn = null;
        try{
            // 建立連線
            URL url =new URL(temp_url);
            conn = (HttpURLConnection)url.openConnection();
            //===============================
            conn.setDoOutput(true);
            // Read from the connection. Default is true.
            conn.setDoInput(true);
            // conn method
            conn.setRequestMethod(method);

            // Post 请求不能使用缓存
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(true);

            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

            //Send request
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            //Get Response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            StringBuffer sb = new StringBuffer();

            while((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            reader.close();
            return sb.toString();
        }catch (Exception e) {
            System.out.println("makeHttpRequest Error:"+e.toString());
        }finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        public ImageDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }
        @Override
        protected Bitmap doInBackground(String... params) {
            return downloadBitmap(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        imageView.setImageDrawable(null);
                    }
                }
            }
        }
    }
    private Bitmap downloadBitmap(String url) {
        HttpURLConnection urlConnection = null;
        try {
            URL uri = new URL(url);
            urlConnection = (HttpURLConnection) uri.openConnection();
            int statusCode = urlConnection.getResponseCode();
            if (statusCode != 200) {
                return null;
            }

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream != null) {
                return BitmapFactory.decodeStream(inputStream);
            }
        } catch (Exception e) {
            Log.w("ImageDownloader", "Error downloading image from " + url);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }


    public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

        private List<Product> contactList;

        public ContactAdapter(List<Product> contactList) {
            this.contactList = contactList;
        }

        @Override
        public int getItemCount() {
            return contactList.size();
        }
        @Override
        public void onBindViewHolder(ContactViewHolder holder, final int position) {
            holder.textView_name.setText(products.get(position).name);
            holder.textView_type.setText(products.get(position).type);
            String price = "$" +  String.format("%,d", products.get(position).price * products.get(position).count );
            holder.textView_count.setText(products.get(position).count + "");
            holder.textView_price.setText( price );
            if (holder.imageView_product != null) {
                if(products.get(position).image_URL != null){
                    new ImageDownloaderTask(holder.imageView_product).execute(products.get(position).image_URL);
                }
            }
        }

        @Override
        public ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.list_product, viewGroup, false);
            return new ContactViewHolder(itemView,i);
        }

        public class ContactViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView_product;
            TextView textView_name;
            TextView textView_type;
            TextView textView_count;
            TextView textView_price;
            TextView textView_plus;
            TextView textView_minus;
            ImageButton button_cancel;
            public ContactViewHolder(View convertView,final int position) {
                super(convertView);
                imageView_product = (ImageView) convertView.findViewById(R.id.product_image);
                textView_name = (TextView) convertView.findViewById(R.id.product_name);
                textView_type = (TextView) convertView.findViewById(R.id.product_type);
                textView_count = (TextView) convertView.findViewById(R.id.product_count);
                textView_price = (TextView) convertView.findViewById(R.id.product_price);
                textView_plus = (TextView) convertView.findViewById(R.id.product_plus);
                textView_minus = (TextView) convertView.findViewById(R.id.product_minus);
                button_cancel = (ImageButton) convertView.findViewById(R.id.product_cancel);
                textView_plus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        Log.d("position",position + "");
                        int count = products.get(position).count;
                        int max = products.get(position).product_max;
                        products.get(position).count = (count == max)?max:count+1;
                        count = products.get(position).count;
                        textView_count.setText(products.get(position).count + "");
                        String price = "$" +  String.format("%,d", products.get(position).price * products.get(position).count );
                        textView_price.setText(price);
                        if(count>1)textView_minus.setTextColor(ShoppingStep1Fragment.this.getResources().getColor(R.color.red_500));
                        if(count == max )textView_plus.setTextColor(ShoppingStep1Fragment.this.getResources().getColor(R.color.grey_500));
                        sum += (count == max)?0: products.get(position).price;
                        products_price.setText("$" + String.format("%,d",sum));
                        products_discount.setText("-$" + String.format("%,d",discount));
                        products_total.setText("$" + String.format("%,d",sum - discount));
                    }
                });
                textView_minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        Log.d("position",position + "");
                        int count = products.get(position).count;
                        products.get(position).count = (count>1)?count-1:1;
                        textView_count.setText(products.get(position).count + "");
                        count = products.get(position).count;
                        String price = "$" +  String.format("%,d", products.get(position).price * products.get(position).count );
                        textView_price.setText(price);
                        if(count == 1)textView_minus.setTextColor(ShoppingStep1Fragment.this.getResources().getColor(R.color.grey_500));
                        if(count < max )textView_plus.setTextColor(ShoppingStep1Fragment.this.getResources().getColor(R.color.red_500));
                        sum -= (count>1)?products.get(position).price:0;
                        products_price.setText("$" + String.format("%,d",sum));
                        products_discount.setText("-$" + String.format("%,d",discount));
                        products_total.setText("$" + String.format("%,d",sum - discount));
                    }
                });
                button_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(products.size()==1);
                        else{
                            int position = getAdapterPosition();
                            Log.d("position",position + "");
                            sum -= products.get(position).price * products.get(position).count;
                            products_price.setText("$" + String.format("%,d",sum));
                            products_discount.setText("-$" + String.format("%,d",discount));
                            products_total.setText("$" + String.format("%,d",sum - discount));
                            products.remove(position);
                            notifyItemRemoved(position);
                        }
                        product_total.setText("共" + products.size() + "項商品");
                    }
                });
            }
        }
    }
}
