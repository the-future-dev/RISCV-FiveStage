main:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	zero,-20(s0)
	li	a5,100
	sw	a5,-24(s0)
	lw	a1,-20(s0)
	li	a0,11
	call	setupmem
	lw	a1,-24(s0)
	li	a0,11
	call	setupmem
	lw	a2,-24(s0)
	lw	a1,-20(s0)
	li	a0,10
	call	f
	li	a5,0
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	nop
	nop
	jr	ra
f:
	addi	sp,sp,-48
	sw	ra,44(sp)
	sw	s0,40(sp)
	sw	s1,36(sp)
	addi	s0,sp,48
	sw	a0,-36(s0)
	sw	a1,-40(s0)
	sw	a2,-44(s0)
	lw	a5,-36(s0)
	slli	a5,a5,2
	lw	a4,-40(s0)
	add	a5,a4,a5
	lw	a5,0(a5)
	nop
	nop
	beqz	a5,.L2
	lw	a5,-36(s0)
	slli	a5,a5,2
	lw	a4,-44(s0)
	add	a5,a4,a5
	lw	a5,0(a5)
	j	.L3
.L2:
	lw	a5,-36(s0)
	nop
	nop
	bnez	a5,.L4
	li	a5,0
	j	.L3
.L4:
	lw	a4,-36(s0)
	li	a5,1
	nop
	bne	a4,a5,.L5
	li	a5,1
	j	.L3
.L5:
	lw	a5,-36(s0)
	addi	a5,a5,-1
	lw	a2,-44(s0)
	lw	a1,-40(s0)
	mv	a0,a5
	call	f
	mv	s1,a0
	lw	a5,-36(s0)
	addi	a5,a5,-2
	lw	a2,-44(s0)
	lw	a1,-40(s0)
	mv	a0,a5
	call	f
	mv	a5,a0
	add	a5,s1,a5
	sw	a5,-20(s0)
	lw	a5,-36(s0)
	slli	a5,a5,2
	lw	a4,-40(s0)
	add	a5,a4,a5
	li	a4,1
	sw	a4,0(a5)
	lw	a5,-36(s0)
	slli	a5,a5,2
	lw	a4,-44(s0)
	add	a5,a4,a5
	lw	a4,-20(s0)
	sw	a4,0(a5)
	lw	a5,-20(s0)
.L3:
	mv	a0,a5
	lw	ra,44(sp)
	lw	s0,40(sp)
	lw	s1,36(sp)
	addi	sp,sp,48
	jr	ra
setupmem:
	addi	sp,sp,-48
	sw	s0,44(sp)
	addi	s0,sp,48
	sw	a0,-36(s0)
	sw	a1,-40(s0)
	sw	zero,-20(s0)
	j	.L7
.L8:
	lw	a5,-20(s0)
	slli	a5,a5,2
	lw	a4,-40(s0)
	add	a5,a4,a5
	sw	zero,0(a5)
	lw	a5,-20(s0)
	addi	a5,a5,1
	sw	a5,-20(s0)
.L7:
	lw	a4,-20(s0)
	lw	a5,-36(s0)
	nop
	nop
	blt	a4,a5,.L8
	nop
	lw	s0,44(sp)
	addi	sp,sp,48
	jr	ra
