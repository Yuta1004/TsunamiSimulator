        // 1. 未来ステップ値計算
        // 1-1. 水平流速更新
        for(int idx = 0; idx < dataSize-1; ++ idx) {
            if(step == 0)
                uf[idx] = up[idx] - grav*(dt/dx)*(zp[idx+1]-zp[idx]);
            else
                uf[idx] = ub[idx] - 2*grav*(dt/dx)*(zp[idx+1]-zp[idx]);
        }
        // 1-2. 海面変位更新
        for(int idx = 1; idx < dataSize-1; ++ idx) {
            double depth0 = (depth[idx]+depth[idx-1]) * 0.5;
            double depth1 = (depth[idx]+depth[idx+1]) * 0.5;
            if(step == 0)
                zf[idx] = zp[idx] - (dx/dx)*(depth1*up[idx]-depth0*up[idx-1]);
            else
                zf[idx] = zb[idx] - 2*(dt/dx)*(depth1*up[idx]-depth0*up[idx-1]);
        }

        // 2. 陸上で流速を0にする
        for(int idx = 0; idx < dataSize-1; ++ idx) {
            if((depth[idx]+depth[idx+1])*0.5 <= 0)
                uf[idx] = 0;
        }

        // 3. 沖側に伝わる津波を強制的に減衰させる
        for(int idx = dataSize-50; idx < dataSize; ++ idx) {
            zf[idx] *= (dataSize-idx)/50.0;
            uf[idx] *= (dataSize-idx)/50.0;
        }

        // 4. 境界条件セット
        zf[0] = 0;
        zf[dataSize-1] = 0;

        // 5. 計算安定化処理
        if(step > 0)
            for(int idx = 0; idx < dataSize; ++ idx) {
                if(idx < dataSize-1)
                    up[idx] += eps * (uf[idx]-2*up[idx]+ub[idx]);
                zp[idx] += eps * (zf[idx]-2*zp[idx]+zb[idx]);
            }

        // 6. ステップ更新
        for(int idx = 0; idx < dataSize; ++ idx) {
            ub[idx] = up[idx];
            up[idx] = uf[idx];
            zb[idx] = zp[idx];
            zp[idx] = zf[idx];
        }

