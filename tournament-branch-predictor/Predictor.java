import java.io.*;
import java.util.HashMap;

public class Predictor {
    private static int f = 0, s = 1, t = 2;
    private static int count = 0;
    private static int s_counter = 0, g_counter = 0, l_counter = 0;
    private static HashMap<Integer, Integer> smap = new HashMap<Integer, Integer>();
    private static HashMap<Integer, Integer> lmap = new HashMap<Integer, Integer>();
    private static HashMap<Integer, Integer> gmap = new HashMap<Integer, Integer>();
    private static int hkey = 000000;


    public static void main(String[] args) throws Exception {
        File f = null, f1 = null;
        if (0 < args.length) {
            f = new File(args[0]);
            f1 = new File(args[1]);
        } else {
            System.err.println("Invalid input");
        }
        Writer fw = new FileWriter(f1);
        BufferedWriter bw = new BufferedWriter(fw);
        scan_input(bw, f);
        System.out.println("Cool! \n");
        System.out.println("The ouput sequence is in the file: " + args[1]);
        System.out.println("The statistics are in the file: statistics.txt");

        bw.close();
    }

    private static char local_pre(int addr) {
        if (lmap.containsKey(addr)) {
            if (lmap.get(addr) > 1) {
                return 't';
            } else {
                return 'n';
            }
        } else {
            return 'n';
        }
    }

    private static void update_local_pre(int addr, char ap) {
        if (lmap.containsKey(addr)) {
            int tc = lmap.get(addr);
            if (ap == 't') {
                if (tc < 3) {
                    tc = tc + 1;
                }
            } else {
                if (tc > 0) {
                    tc = tc - 1;
                }
            }
            lmap.put(addr, tc);
        } else {
            if (ap == 't') {
                lmap.put(addr, 1);
            } else {
                lmap.put(addr, 0);
            }
        }
    }

    private static void scan_input(BufferedWriter bw, File file1) throws Exception {
        //String in = "0n11t77n88t33n44t55t88t33n44n55t88n99t00t22t11t77n88t33n44t55t88t33n44n55t88n99t00t22t11t77n88t33n44t55t88t3";
        BufferedReader br = new BufferedReader(new FileReader(file1));
        String min = null;
        StringBuilder in = new StringBuilder();
        while ((min = br.readLine()) != null) {
            in.append(min);
        }
        for (int i = 1; i <= in.length() / 3; i++) {
            int addr = Character.getNumericValue(in.charAt(f));
            char actual_pre = in.charAt(s);
            int next_addr = Character.getNumericValue(in.charAt(t));
            f = f + 3;
            s = s + 3;
            t = t + 3;
            char local_prediction = local_pre(addr);
            char global_prediction = global_pre();
            char main_selector = selector(addr);
            char main_prediction;
            if (main_selector == 'g') {
                main_prediction = global_prediction;
            } else {
                main_prediction = local_prediction;
            }

            if (global_prediction != local_prediction) {
                if (global_prediction == actual_pre) {
                    update_selector(addr, 'g');
                } else {
                    update_selector(addr, 'l');
                }
            }

            if (actual_pre == local_prediction) {
                l_counter = l_counter + 1;
            }
            if (actual_pre == global_prediction) {
                g_counter = g_counter + 1;
            }
            if (actual_pre == main_prediction) {
                s_counter = s_counter + 1;
            }
            update_global_pre(next_addr, actual_pre);
            update_local_pre(addr, actual_pre);
            output(bw, addr, local_prediction, global_prediction, main_selector, main_prediction, actual_pre);
        }
        //System.out.println("Local prediction hits: " + l_counter + "\n Global prediction hits: " + g_counter + "\n Tournament prediction hits: " + s_counter);
        PrintWriter writer = new PrintWriter("statistics.txt", "UTF-8");
        writer.println("Local prediction hits: " + l_counter);
        writer.println("Global prediction hits: " + g_counter);
        writer.println("Tournament prediction hits: " + s_counter);
        writer.close();

    }

    private static void update_selector(int addr, char sel) {
        // local selector =  0 1; global selector = 2 3
        if (sel == 'g') {
            if (smap.containsKey(addr)) {
                int tc = smap.get(addr);
                if (tc < 3) {
                    tc = tc + 1;
                    smap.put(addr, tc);
                }
            } else {
                smap.put(addr, 1);
            }
        } else {
            if (smap.containsKey(addr)) {
                int tc = smap.get(addr);
                if (tc > 0) {
                    tc = tc - 1;
                    smap.put(addr, tc);
                }
            } else {
                smap.put(addr, 0);
            }
        }
    }


    private static char selector(int addr) {
        if (smap.containsKey(addr)) {
            if (smap.get(addr) > 1) {
                return 'g';
            } else {
                return 'l';
            }
        }
        return 'l';
    }

    private static char global_pre() {
        if (gmap.containsKey(hkey)) {
            if (gmap.get(hkey) > 1) {
                return 't';
            } else {
                return 'n';
            }
        } else {
            return 'n';
        }
    }

    private static void update_global_pre(int nxt_addr, char ap) {
        int shift_ap;
        if (ap == 't') {
            shift_ap = 1;
            if (gmap.containsKey(hkey)) {
                int tc = gmap.get(hkey);
                if (tc < 3) {
                    tc = tc + 1;
                    gmap.put(hkey, tc);
                }
            } else {
                gmap.put(hkey, 1);
            }

        } else {
            shift_ap = 0;
            if (gmap.containsKey(hkey)) {
                int tc = gmap.get(hkey);
                if (tc > 0) {
                    tc = tc - 1;
                    gmap.put(hkey, tc);
                }
            } else {
                gmap.put(hkey, 0);
            }
        }
        shift(shift_ap);
    }

    private static void shift(int s) {
        String temp1 = Integer.toString(hkey);
        StringBuilder sb = new StringBuilder(temp1);
        if (sb.length() == 6) {
            sb.deleteCharAt(0);
        }
        String temp2 = Integer.toString(s);
        hkey = Integer.parseInt(sb + temp2);
    }

    private static void output(BufferedWriter bw, int addr, char local_prediction, char global_prediction, char main_selector, char main_prediction, char actual_pre) throws Exception {
        //System.out.println("" + addr + local_prediction + global_prediction + main_selector + main_prediction + actual_pre);
        bw.write("" + addr + local_prediction + global_prediction + main_selector + main_prediction + actual_pre + "\n");
    }
}
