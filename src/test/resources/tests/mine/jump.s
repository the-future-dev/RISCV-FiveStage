main: addi x30, x30, 0
      jal x31, m1
m2:   addi x30, x30, 2
      jal x2, mx
m1:   addi x30, x30, 1
      jal x1, m2
mx:   sw x30, 0(zero)
      done
#memset 0x30,  4