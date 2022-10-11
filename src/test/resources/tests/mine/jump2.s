main: 
      addi x2, zero, 5
      addi x1, zero, 0
      jal x31, .s
.e:   
      done
.s:   
      addi x1, x1, 1
      beq x1, x2, .e
      jal x31, .s
      done